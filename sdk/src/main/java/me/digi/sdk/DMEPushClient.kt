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
import me.digi.sdk.api.helpers.DMEMultipartBody
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager
import me.digi.sdk.interapp.managers.SaasConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEDataEncryptor
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.*
import java.security.PrivateKey

class DMEPushClient(
    val context: Context,
    val configuration: DMEPushConfiguration
) :
    DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy {
        DMEPostboxConsentManager(sessionManager, configuration.appId)
    }
    private val authConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(
            configuration.baseUrl, type = "authorize"
        )
    }

    private val authorizeManger: SaasConsentManager by lazy {
        SaasConsentManager(
            configuration.baseUrl,
            "authorize"
        )
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    fun createPostbox(fromActivity: Activity, completion: DMEPostboxCreationCompletion) {

        DMELog.i("Launching user consent request.")

        val req = DMESessionRequest(
            configuration.appId,
            configuration.contractId,
            DMESDKAgent(),
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

    fun authorize(fromActivity: Activity, completion: AuthorizationCompletion) {

        fun requestPreAuthCode(): Single<Pair<Session, Payload>> = Single.create { emitter ->

            val codeVerifier =
                DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))

            val jwt = DMEPreauthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.getPreAuthorizationCode(authHeader)) { response, error ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload = Gson().fromJson(payloadJson, Payload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] =
                            codeVerifier

                        val result = Pair(response.session, payload)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity): SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> =
            SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> {
                it.flatMap { response ->
                    Single.create { emitter ->
                        response.second.preAuthorizationCode?.let {
                            authConsentManager.beginConsentAction(
                                fromActivity,
                                it
                            ) { authSession, error ->
                                when {
                                    error != null -> emitter.onError(error)
                                    authSession != null -> emitter.onSuccess(
                                        Pair(response.first, authSession)
                                    )
                                    else -> emitter.onError(IllegalArgumentException())
                                }
                            }
                        }
                    }
                }
            }

        fun exchangeAuthorizationCode(): SingleTransformer<Pair<Session, AuthSession>, DMESaasOngoingPostbox> =
            SingleTransformer<Pair<Session, AuthSession>, DMESaasOngoingPostbox> {
                it.flatMap { response: Pair<Session, AuthSession> ->

                    val codeVerifier =
                        response.first.metadata[context.getString(R.string.key_code_verifier)].toString()

                    val jwt = DMEAuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        response.second.code!!,
                        codeVerifier
                    )

                    val signingKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { exchangeToken: ExchangeTokenJWT ->

                            val chunks: List<String> = exchangeToken.token.split(".")
                            val payloadJson: String =
                                String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange =
                                Gson().fromJson(payloadJson, DMETokenExchange::class.java)

                            val postboxData = DMEOngoingPostboxData().copy(
                                postboxId = response.second.postboxId,
                                publicKey = response.second.publicKey
                            )

                            DMESaasOngoingPostbox(response.first, postboxData, tokenExchange)
                        }
                }
            }

        requestPreAuthCode()
            .compose(requestConsent(fromActivity))
            .compose(exchangeAuthorizationCode())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: DMESaasOngoingPostbox ->
                    sessionManager.newSession = result.session
                    println("Session: ${result.session}")
                    println("Postbox: ${result.postboxData}")
                    println("Token: ${result.authToken}")
                    val authSession = AuthSession().copy(
                        postboxId = result.postboxData?.postboxId,
                        publicKey = result.postboxData?.publicKey,
                        sessionKey = result.session?.key,
                        accessToken = result.authToken?.accessToken?.value
                    )
                    completion.invoke(authSession, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError } ?: DMEAPIError.GENERIC(
                            0,
                            error.localizedMessage
                        )
                    )
                }
            )
            .addTo(disposable)
    }

    fun pushDataToPostbox(postboxFile: DMEPushPayload, completion: DMEPostboxPushCompletion) {
        DMELog.i("Initializing push data to postbox.")

        if (sessionManager.isSessionValid()) {
            val encryptedData = DMEDataEncryptor.encryptedDataFromBytes(
                postboxFile.dmePostbox.publicKey!!,
                postboxFile.content,
                postboxFile.metadata
            )

            val multipartBody = DMEMultipartBody.Builder()
                .postboxPushPayload(postboxFile)
                .dataContent(encryptedData.fileContent, postboxFile.mimeType)
                .build()

            apiClient.makeCall(
                apiClient.argonService.pushData(
                    postboxFile.dmePostbox.key!!,
                    encryptedData.symmetricalKey,
                    encryptedData.iv,
                    encryptedData.metadata,
                    postboxFile.dmePostbox.postboxId!!,
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

    fun pushDataToOngoingPostbox(
        postboxFile: DMEPushPayload?,
        authToken: DMETokenExchange?,
        completion: DMEOngoingPostboxPushCompletion
    ) {
        DMELog.i("Initializing push data to postbox.")

        val postboxFile: DMEPushPayload = postboxFile!!
        val authToken: DMETokenExchange = authToken!!

        if (sessionManager.isSessionValid()) {
            val encryptedData = DMEDataEncryptor.encryptedDataFromBytes(
                postboxFile.dmePostbox.publicKey!!,
                postboxFile.content,
                postboxFile.metadata
            )

            val multipartBody: DMEMultipartBody = DMEMultipartBody.Builder()
                .postboxPushPayload(postboxFile)
                .dataContent(encryptedData.fileContent, postboxFile.mimeType)
                .build()

            val jwt = DMEAuthTokenRequestJWT(
                authToken.accessToken.value,
                encryptedData.iv,
                encryptedData.metadata,
                encryptedData.symmetricalKey,
                configuration.appId,
                configuration.contractId
            )

            val signingKey: PrivateKey =
                DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.argonService.pushOngoingData(
                authHeader,
                postboxFile.dmePostbox.key!!,
                encryptedData.symmetricalKey,
                encryptedData.iv,
                encryptedData.metadata,
                postboxFile.dmePostbox.postboxId!!,
                multipartBody.requestBody,
                multipartBody.description
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        sessionManager.newSession = it.session
                        completion(
                            it,
                            null
                        ).also { DMELog.i("Successfully pushed data to postbox") }
                    },
                    onError = { error ->
                        when {
                            error is DMEAPIError && error.code == "InvalidToken" -> completion(
                                null,
                                DMEAPIError.GENERIC(message = "Failed to push file to postbox. Access token is invalid. Request new session.")
                            )
                            else -> {
                                DMELog.e("Failed to push file to postbox. Error: ${error.printStackTrace()} ${error.message}")
                                completion(
                                    null,
                                    DMEAuthError.ErrorWithMessage(error.localizedMessage)
                                )
                            }
                        }
                    }
                )
        } else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    fun authorizeOngoingPostbox(
        fromActivity: Activity,
        existingPostbox: DMEOngoingPostboxData? = null,
        credentials: DMETokenExchange? = null,
        completion: DMESaasPostboxOngoingCreationCompletion
    ) {

        // Defined bellow are a number of 'modules' that are used within the Cyclic Postbox flow.
        // These can be combined in various ways as the auth state demands.
        // See the flow below for details.

        fun requestPreAuthCode(): Single<Pair<Session, Payload>> = Single.create { emitter ->

            val codeVerifier =
                DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))

            val jwt = DMEPreauthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.getPreAuthorizationCode(authHeader)) { response, error ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload = Gson().fromJson(payloadJson, Payload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] =
                            codeVerifier

                        val result: Pair<Session, Payload> = Pair(response.session, payload)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity): SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> =
            SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> {
                it.flatMap { response ->
                    Single.create { emitter ->
                        response.second.preAuthorizationCode?.let {
                            authorizeManger.beginConsentAction(
                                fromActivity,
                                it
                            ) { authSession, error ->
                                when {
                                    error != null -> emitter.onError(error)
                                    authSession != null -> emitter.onSuccess(
                                        Pair(
                                            response.first,
                                            authSession
                                        )
                                    )
                                    else -> emitter.onError(IllegalArgumentException())
                                }
                            }
                        }
                    }
                }
            }

        fun exchangeAuthorizationCode(): SingleTransformer<Pair<Session, AuthSession>, DMESaasOngoingPostbox> =
            SingleTransformer<Pair<Session, AuthSession>, DMESaasOngoingPostbox> {
                it.flatMap { response: Pair<Session, AuthSession> ->

                    val codeVerifier =
                        response.first.metadata[context.getString(R.string.key_code_verifier)].toString()

                    val jwt = DMEAuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        response.second.code!!,
                        codeVerifier
                    )

                    val signingKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { exchangeToken: ExchangeTokenJWT ->

                            val chunks: List<String> = exchangeToken.token.split(".")
                            val payloadJson: String =
                                String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange =
                                Gson().fromJson(payloadJson, DMETokenExchange::class.java)

                            val postboxData = DMEOngoingPostboxData().copy(
                                postboxId = response.second.postboxId,
                                publicKey = response.second.publicKey
                            )

                            DMESaasOngoingPostbox(response.first, postboxData, tokenExchange)
                        }
                }
            }

        fun refreshCredentials(): SingleTransformer<DMESaasOngoingPostbox, DMESaasOngoingPostbox> =
            SingleTransformer<DMESaasOngoingPostbox, DMESaasOngoingPostbox> {
                it.flatMap { result ->

                    val jwt = RefreshCredentialsRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        result.authToken?.refreshToken?.value!!
                    )

                    val signingKey: PrivateKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                        .map { exchangeToken ->

                            val chunks: List<String> = exchangeToken.token.split(".")
                            val payloadJson: String =
                                String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange =
                                Gson().fromJson(payloadJson, DMETokenExchange::class.java)

                            DMESaasOngoingPostbox(result.session, result.postboxData, tokenExchange)
                        }
                }
            }

        var activeCredentials: DMETokenExchange? = credentials
        var activePostbox: DMEOngoingPostboxData? = existingPostbox

        // First, we request pre-auth code needed for auth consent manager
        requestPreAuthCode()
            // Next we check if any credentials were supplied (for access restoration)
            // If not, we kick the user out of the flow and authorize normally
            .let { preAuthResponse ->
                if (activeCredentials != null && activePostbox != null) {
                    preAuthResponse.map {
                        DMESaasOngoingPostbox(
                            it.first,
                            activePostbox,
                            activeCredentials
                        )
                    }
                } else {
                    preAuthResponse
                        .compose(requestConsent(fromActivity))
                        .compose(exchangeAuthorizationCode())
                        .doOnSuccess {
                            activePostbox = it.postboxData
                            activeCredentials = it.authToken
                        }
                }
            }
            // At this point, we have a session, postbox and a set of credentials
            .onErrorResumeNext { error: Throwable ->
                // If an error is encountered from this call, we inspect it to see if it's an 'InternalServerError'
                // error, meaning that implicit sync was triggered wor a removed deviceId (library changed).
                // We process the consent flow for ongoing access
                when {
                    error is DMEAPIError && error.code == "InternalServerError" -> {

                        requestPreAuthCode()
                            .compose(requestConsent(fromActivity))
                            .compose(exchangeAuthorizationCode())
                            .doOnSuccess {
                                activePostbox = it.postboxData
                                activeCredentials = it.authToken
                            }

                        // If an error we encountered is a "InvalidToken" error, which means that the ACCESS token
                        // has expired.
                    }
                    // If an error we encounter is "InvalidToken" error, which means that the ACCESS token
                    // has expired.
                    error is DMEAPIError && error.code == "InvalidToken" -> {
                        // If so, we take the active session and expired credentials and try to refresh them.

                        requestPreAuthCode()
                            .map {
                                DMESaasOngoingPostbox(
                                    it.first,
                                    activePostbox,
                                    activeCredentials
                                )
                            }
                            .compose(refreshCredentials())
                            .doOnSuccess { activeCredentials = it.authToken }
                            .onErrorResumeNext { innerError: Throwable ->

                                // If an error is encountered from this call, we inspect it to see if it's an
                                // 'InvalidToken' error, meaning that the REFRESH token has expired.
                                if (innerError is DMEAPIError && error.code == "InvalidToken") {
                                    // If so, we need to obtain a new set of credentials from the digi.me
                                    // application. Process the flow as before, for ongoing acces, provided
                                    // that auto-recover is enabled. If not, we throw a specific error and
                                    // exit the flow.
                                    if (configuration.autoRecoverExpiredCredentials) {
                                        requestPreAuthCode()
                                            .compose(requestConsent(fromActivity))
                                            .compose(exchangeAuthorizationCode())
                                            .doOnSuccess {
                                                activePostbox = it.postboxData
                                                activeCredentials = it.authToken
                                            }
                                    } else Single.error(DMEAuthError.TokenExpired())
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
                    println("Data: $it")
                    sessionManager.newSession = it.session
                    completion.invoke(it, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError } ?: DMEAPIError.GENERIC(
                            0,
                            error.localizedMessage
                        ))
                }
            )
            .addTo(disposable)
    }
}

