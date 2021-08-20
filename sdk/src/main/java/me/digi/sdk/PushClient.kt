package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.sdk.api.helpers.MultipartBody
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.configuration.WriteConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.PreAuthorizationCodePayload
import me.digi.sdk.entities.request.AuthorizationScopeRequest
import me.digi.sdk.entities.request.SessionRequest
import me.digi.sdk.entities.response.AuthorizeResponse
import me.digi.sdk.entities.response.SessionResponse
import me.digi.sdk.entities.response.TokenResponse
import me.digi.sdk.interapp.managers.PostboxConsentManager
import me.digi.sdk.interapp.managers.SaasConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import me.digi.sdk.utilities.crypto.DataEncryptor
import me.digi.sdk.utilities.crypto.KeyTransformer
import me.digi.sdk.utilities.jwt.*
import java.security.PrivateKey

class PushClient(
    val context: Context,
    val configuration: WriteConfiguration
) :
    Client(context, configuration) {

    private val postboxConsentManager: PostboxConsentManager by lazy {
        PostboxConsentManager(sessionManager, configuration.appId)
    }
    private val authorizeManger: SaasConsentManager by lazy {
        SaasConsentManager(configuration.baseUrl, "authorize")
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    fun updateSession(sessionRequest: SessionRequest, completion: GetSessionCompletion) {

        fun requestSession(sessionRequest: SessionRequest): Single<SessionResponse> =
            Single.create { emitter ->
                apiClient.makeCall(apiClient.argonService.getSession(sessionRequest)) { sessionResponse, error ->
                    when {
                        sessionResponse != null -> emitter.onSuccess(sessionResponse)
                        error != null -> emitter.onError(error)
                        else -> emitter.onError(IllegalArgumentException())
                    }
                }
            }

        requestSession(sessionRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    val session = Session().copy(key = it.key, expiry = it.expiry)
                    sessionManager.updatedSession = session
                    completion.invoke(true, null)
                },
                onError = {
                    completion.invoke(
                        false,
                        AuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    fun createPostbox(fromActivity: Activity, completion: PostboxCreationCompletion) {

        DMELog.i("Launching user consent request.")

        val req = SessionRequest(
            configuration.appId,
            configuration.contractId,
            SdkAgent(),
            "gzip",
            null
        )
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                postboxConsentManager.beginPostboxAuthorization(fromActivity, completion)
            } else {
                DMELog.e("An error occurred whilst communicating with our servers: ${error?.message}")
                completion(null, error)
            }
        }
    }

    fun pushDataToPostbox(postboxFile: DataPayload, completion: PostboxPushCompletion) {
        DMELog.i("Initializing push data to postbox.")

        if (sessionManager.isSessionValid()) {
            val encryptedData = DataEncryptor.encryptedDataFromBytes(
                postboxFile.data.publicKey!!,
                postboxFile.content,
                postboxFile.metadata
            )

            val multipartBody = MultipartBody.Builder()
                .postboxPushPayload(postboxFile)
                .dataContent(encryptedData.fileContent, postboxFile.mimeType)
                .build()

            apiClient.makeCall(
                apiClient.argonService.pushData(
                    postboxFile.data.key!!,
                    encryptedData.symmetricalKey,
                    encryptedData.iv,
                    encryptedData.metadata,
                    postboxFile.data.postboxId!!,
                    multipartBody.requestBody,
                    multipartBody.description
                )
            ) { _, error ->

                if (error != null) {
                    DMELog.e("Failed to push file to postbox. Error: ${error.message}")
                    completion(error)
                }

                DMELog.i("Successfully pushed data to postbox")
                completion(null)
            }
        }
    }

    fun authorize(
        fromActivity: Activity,
        accessToken: String? = null,
        serviceId: String? = null,
        completion: AuthCompletion
    ) {

        fun requestPreAuthCode(): Single<GetPreAuthCodeDone> = Single.create { emitter ->

            val codeVerifier =
                ByteTransformer.hexStringFromBytes(CryptoUtilities.generateSecureRandom(64))

            val jwt = if (accessToken != null)
                PreAuthorizationRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    codeVerifier,
                    accessToken
                ) else PreAuthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient.argonService.getPreAuthorizationCode(
                    authHeader,
                    AuthorizationScopeRequest()
                )
            ) { response, error ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload: PreAuthorizationCodePayload =
                            Gson().fromJson(payloadJson, PreAuthorizationCodePayload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] =
                            codeVerifier

                        val result =
                            GetPreAuthCodeDone().copy(session = response.session, payload = payload)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity): SingleTransformer<GetPreAuthCodeDone, GetConsentDone> =
            SingleTransformer<GetPreAuthCodeDone, GetConsentDone> {
                it.flatMap { response ->
                    Single.create { emitter ->
                        response.payload.preAuthorizationCode?.let { code ->
                            authorizeManger.beginConsentAction(
                                fromActivity,
                                code,
                                serviceId
                            ) { authSession, error ->
                                when {
                                    authSession != null -> {
                                        val consentDone = GetConsentDone().copy(
                                            session = response.session,
                                            consentResponse = authSession
                                        )
                                        emitter.onSuccess(consentDone)
                                    }
                                    error != null -> emitter.onError(error)
                                    else -> emitter.onError(IllegalArgumentException())
                                }
                            }
                        }
                    }
                }
            }

        fun exchangeAuthorizationCode(): SingleTransformer<GetConsentDone, OngoingData> =
            SingleTransformer<GetConsentDone, OngoingData> {
                it.flatMap { response: GetConsentDone ->

                    val codeVerifier =
                        response.session.metadata[context.getString(R.string.key_code_verifier)].toString()

                    val jwt = AuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        response.consentResponse.code!!,
                        codeVerifier
                    )

                    val signingKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { token: TokenResponse ->

                            val chunks: List<String> = token.token.split(".")
                            val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange: CredentialsPayload =
                                Gson().fromJson(payloadJson, CredentialsPayload::class.java)

                            val postboxData = WriteDataInfoPayload().copy(
                                postboxId = response.consentResponse.postboxId,
                                publicKey = response.consentResponse.publicKey
                            )

                            OngoingData(response.session, postboxData, tokenExchange)
                        }
                }
            }

        requestPreAuthCode()
            .compose(requestConsent(fromActivity))
            .compose(exchangeAuthorizationCode())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: OngoingData ->
                    sessionManager.updatedSession = result.session

                    val authSession = AuthorizeResponse().copy(
                        postboxId = result.data?.postboxId,
                        publicKey = result.data?.publicKey,
                        sessionKey = result.session?.key,
                        accessToken = result.credentials?.accessToken?.value
                    )

                    completion.invoke(authSession, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? Error } ?: APIError.GENERIC(
                            0,
                            error.localizedMessage
                        )
                    )
                }
            )
            .addTo(disposable)
    }

    fun authorizeOngoingPostbox(
        fromActivity: Activity,
        existingPostbox: WriteDataInfoPayload? = null,
        credentials: CredentialsPayload? = null,
        serviceId: String? = null,
        completion: SaasPostboxOngoingCreationCompletion
    ) {

        // Defined bellow are a number of 'modules' that are used within the Cyclic Postbox flow.
        // These can be combined in various ways as the auth state demands.
        // See the flow below for details.

        fun requestPreAuthCode(): Single<GetPreAuthCodeDone> = Single.create { emitter ->

            val codeVerifier =
                ByteTransformer.hexStringFromBytes(CryptoUtilities.generateSecureRandom(64))

            val jwt = if (credentials != null)
                PreAuthorizationRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    codeVerifier,
                    credentials.accessToken.value
                )
            else PreAuthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient.argonService.getPreAuthorizationCode(
                    authHeader,
                    AuthorizationScopeRequest()
                )
            ) { response, error ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload: PreAuthorizationCodePayload =
                            Gson().fromJson(payloadJson, PreAuthorizationCodePayload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] =
                            codeVerifier

                        val result =
                            GetPreAuthCodeDone().copy(session = response.session, payload = payload)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity): SingleTransformer<GetPreAuthCodeDone, GetConsentDone> =
            SingleTransformer<GetPreAuthCodeDone, GetConsentDone> {
                it.flatMap { response ->
                    Single.create { emitter ->
                        response.payload.preAuthorizationCode?.let { code ->
                            authorizeManger.beginConsentAction(
                                fromActivity,
                                code,
                                serviceId
                            ) { authSession, error ->
                                when {
                                    authSession != null -> {
                                        val consentDone = GetConsentDone().copy(
                                            session = response.session,
                                            consentResponse = authSession
                                        )
                                        emitter.onSuccess(consentDone)
                                    }
                                    error != null -> emitter.onError(error)
                                    else -> emitter.onError(IllegalArgumentException())
                                }
                            }
                        }
                    }
                }
            }

        fun exchangeAuthorizationCode(): SingleTransformer<GetConsentDone, OngoingData> =
            SingleTransformer<GetConsentDone, OngoingData> {
                it.flatMap { response: GetConsentDone ->

                    val codeVerifier =
                        response.session.metadata[context.getString(R.string.key_code_verifier)].toString()

                    val jwt = AuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        response.consentResponse.code!!,
                        codeVerifier
                    )

                    val signingKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { token: TokenResponse ->

                            val chunks: List<String> = token.token.split(".")
                            val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange: CredentialsPayload =
                                Gson().fromJson(payloadJson, CredentialsPayload::class.java)

                            val postboxData = WriteDataInfoPayload().copy(
                                postboxId = response.consentResponse.postboxId,
                                publicKey = response.consentResponse.publicKey
                            )

                            OngoingData(response.session, postboxData, tokenExchange)
                        }
                }
            }

        fun refreshCredentials(): SingleTransformer<OngoingData, OngoingData> =
            SingleTransformer<OngoingData, OngoingData> {
                it.flatMap { result ->

                    val jwt = RefreshCredentialsRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        result.credentials?.refreshToken?.value!!
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                        .map { exchangeToken ->

                            val chunks: List<String> = exchangeToken.token.split(".")
                            val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange: CredentialsPayload =
                                Gson().fromJson(payloadJson, CredentialsPayload::class.java)

                            OngoingData().copy(
                                session = result.session,
                                data = result.data,
                                credentials = tokenExchange
                            )
                        }
                }
            }

        var activeCredentials: CredentialsPayload? = credentials
        var activePostbox: WriteDataInfoPayload? = existingPostbox

        // First, we request pre-auth code needed for auth consent manager
        requestPreAuthCode()
            // Next we check if any credentials were supplied (for access restoration)
            // If not, we kick the user out of the flow and authorize normally
            .let { preAuthResponse ->
                if (activeCredentials != null && activePostbox != null) {
                    preAuthResponse.map {
                        OngoingData(
                            it.session,
                            activePostbox,
                            activeCredentials
                        )
                    }
                } else {
                    preAuthResponse
                        .compose(requestConsent(fromActivity))
                        .compose(exchangeAuthorizationCode())
                        .doOnSuccess {
                            activePostbox = it.data
                            activeCredentials = it.credentials
                        }
                }
            }
            // At this point, we have a session, postbox and a set of credentials
            .onErrorResumeNext { error: Throwable ->
                // If an error is encountered from this call, we inspect it to see if it's an 'InternalServerError'
                // error, meaning that implicit sync was triggered wor a removed deviceId (library changed).
                // We process the consent flow for ongoing access
                when {
                    error is APIError && error.code == "InternalServerError" -> {

                        requestPreAuthCode()
                            .compose(requestConsent(fromActivity))
                            .compose(exchangeAuthorizationCode())
                            .doOnSuccess {
                                activePostbox = it.data
                                activeCredentials = it.credentials
                            }

                        // If an error we encountered is a "InvalidToken" error, which means that the ACCESS token
                        // has expired.
                    }
                    // If an error we encounter is "InvalidToken" error, which means that the ACCESS token
                    // has expired.
                    error is APIError && error.code == "InvalidToken" -> {
                        // If so, we take the active session and expired credentials and try to refresh them.

                        requestPreAuthCode()
                            .map {
                                OngoingData(
                                    it.session,
                                    activePostbox,
                                    activeCredentials
                                )
                            }
                            .compose(refreshCredentials())
                            .doOnSuccess { activeCredentials = it.credentials }
                            .onErrorResumeNext { innerError: Throwable ->

                                // If an error is encountered from this call, we inspect it to see if it's an
                                // 'InvalidToken' error, meaning that the REFRESH token has expired.
                                if (innerError is APIError && error.code == "InvalidToken") {
                                    // If so, we need to obtain a new set of credentials from the digi.me
                                    // application. Process the flow as before, for ongoing access, provided
                                    // that auto-recover is enabled. If not, we throw a specific error and
                                    // exit the flow.
                                    if (configuration.autoRecoverExpiredCredentials) {
                                        requestPreAuthCode()
                                            .compose(requestConsent(fromActivity))
                                            .compose(exchangeAuthorizationCode())
                                            .doOnSuccess {
                                                activePostbox = it.data
                                                activeCredentials = it.credentials
                                            }
                                    } else Single.error(AuthError.TokenExpired())
                                } else Single.error(innerError)
                            }
                    }
                    else -> Single.error(error)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    sessionManager.updatedSession = it.session
                    completion.invoke(it, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? Error } ?: APIError.GENERIC(
                            0,
                            error.localizedMessage
                        ))
                }
            )
            .addTo(disposable)
    }

    fun pushData(
        postboxFile: DataPayload?,
        accessToken: String,
        completion: OngoingWriteCompletion
    ) {
        DMELog.i("Initializing push data to postbox.")

        val postbox = postboxFile as DataPayload

        if (sessionManager.isSessionValid()) {
            val encryptedData = DataEncryptor.encryptedDataFromBytes(
                postbox.data.publicKey!!,
                postbox.content,
                postbox.metadata
            )

            val multipartBody: MultipartBody = MultipartBody.Builder()
                .postboxPushPayload(postbox)
                .dataContent(encryptedData.fileContent, postbox.mimeType)
                .build()

            val jwt = AuthTokenRequestJWT(
                accessToken,
                encryptedData.iv,
                encryptedData.metadata,
                encryptedData.symmetricalKey,
                configuration.appId,
                configuration.contractId
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.argonService.pushOngoingData(
                authHeader,
                postbox.data.key!!,
                encryptedData.symmetricalKey,
                encryptedData.iv,
                encryptedData.metadata,
                postbox.data.postboxId!!,
                multipartBody.requestBody,
                multipartBody.description
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        sessionManager.updatedSession = it.session
                        completion(
                            it,
                            null
                        ).also { DMELog.i("Successfully pushed data to postbox") }
                    },
                    onError = { error ->
                        when {
                            error is APIError && error.code == "InvalidToken" -> completion(
                                null,
                                APIError.GENERIC(message = "Failed to push file to postbox. Access token is invalid. Request new session.")
                            )
                            else -> {
                                DMELog.e("Failed to push file to postbox. Error: ${error.printStackTrace()} ${error.message}")
                                completion(
                                    null,
                                    AuthError.ErrorWithMessage(error.localizedMessage)
                                )
                            }
                        }
                    }
                )
        } else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, AuthError.InvalidSession())
        }
    }

    fun deleteUser(accessToken: String?, completion: UserDeleteCompletion) {
        DMELog.i(context.getString(R.string.labelDeleteLibrary))

        fun deleteLibrary() = Single.create<Boolean> { emitter ->
            accessToken?.let {

                val jwt = UserDeletionRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    accessToken
                )

                val signingKey: PrivateKey =
                    KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                val authHeader: String = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.deleteUser(authHeader)) { _, error ->
                    when {
                        error != null -> emitter.onError(error)
                        else -> emitter.onSuccess(true)
                    }
                }
            }
                ?: emitter.onError(APIError.ErrorWithMessage(context.getString(R.string.labelAccessTokenInvalidOrMissing)))
        }

        deleteLibrary()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    completion.invoke(it, null)
                },
                onError = {
                    it.localizedMessage?.let { message ->
                        if (message.contains("204"))
                            completion.invoke(true, null)
                        else completion.invoke(
                            null,
                            APIError.ErrorWithMessage(message)
                        )
                    }
                }
            )
    }
}

