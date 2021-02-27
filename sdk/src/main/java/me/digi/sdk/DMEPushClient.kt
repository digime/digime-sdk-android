package me.digi.sdk

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.sdk.api.helpers.DMEMultipartBody
import me.digi.sdk.callbacks.DMEOngoingPostboxPushCompletion
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.callbacks.DMEPostboxOngoingCreationCompletion
import me.digi.sdk.callbacks.DMEPostboxPushCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.interapp.managers.DMEOngoingPostboxConsentManager
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEDataEncryptor
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.*

class DMEPushClient(val context: Context, val configuration: DMEPushConfiguration) :
    DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy {
        DMEPostboxConsentManager(sessionManager, configuration.appId)
    }

    private val postboxOngoingManager: DMEOngoingPostboxConsentManager by lazy {
        DMEOngoingPostboxConsentManager(sessionManager, configuration.appId)
    }

    private val compositeDisposable = CompositeDisposable()

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

    fun createOngoingPostbox(
        fromActivity: Activity,
        existingPostbox: DMEPostbox?,
        credentials: DMEOAuthToken?,
        completion: DMEPostboxOngoingCreationCompletion
    ) {

        fun requestSession(request: DMESessionRequest) = Single.create<DMESession> { emitter ->
            sessionManager.getSession(request) { session, error ->
                when {
                    session != null -> emitter.onSuccess(session)
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        fun requestPreAuthorizationCode(): SingleTransformer<DMESession, DMESession> =
            SingleTransformer {
                it.flatMap { session ->
                    val codeVerifier = DMEByteTransformer.hexStringFromBytes(
                        DMECryptoUtilities.generateSecureRandom(64)
                    )
                    session.metadata[context.getString(R.string.key_code_verifier)] = codeVerifier

                    val jwt = DMEPreauthorizationRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        codeVerifier
                    )

                    val signingKey =
                        DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.getPreauthorizationCode(authHeader))
                        .map {
                            session.apply {
                                preauthorizationCode = it.preauthorizationCode
                            }
                        }
                }
            }

        fun requestConsent(fromActivity: Activity): SingleTransformer<DMESession, Pair<DMESession, DMEPostbox?>> =
            SingleTransformer {
                it.flatMap { result ->
                    Single.create<Pair<DMESession, DMEPostbox?>> { emitter ->
                        when (Pair(DMEAppCommunicator.getSharedInstance().canOpenDMEApp(), false)) {
                            Pair(
                                true,
                                false
                            ) -> postboxOngoingManager.beginOngoingPostboxAuthorization(fromActivity) { session, postbox, error ->
                                when {
                                    session != null -> emitter.onSuccess(Pair(session, postbox))
                                    error != null -> emitter.onError(error)
                                    else -> emitter.onError(java.lang.IllegalArgumentException())
                                }
                            }
                            Pair(false, false) -> {
                                DMEAppCommunicator.getSharedInstance()
                                    .requestInstallOfDMEApp(fromActivity) {
                                        postboxOngoingManager.beginOngoingPostboxAuthorization(
                                            fromActivity
                                        ) { session, postbox, error ->
                                            when {
                                                session != null -> emitter.onSuccess(
                                                    Pair(
                                                        session,
                                                        postbox
                                                    )
                                                )
                                                error != null -> emitter.onError(error)
                                                else -> emitter.onError(java.lang.IllegalArgumentException())
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }

        fun exchangeAuthorizationCode(): SingleTransformer<Pair<DMESession, DMEPostbox?>, ExchangeResponse> =
            SingleTransformer {
                it.flatMap { result ->

                    val codeVerifier =
                        result.first.metadata[context.getString(R.string.key_code_verifier)].toString()
                    val jwt = DMEAuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        result.first.authorizationCode!!,
                        codeVerifier
                    )
                    val signingKey =
                        DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { token: DMEAuthCodeExchangeResponseJWT ->
                            ExchangeResponse(
                                result.first,
                                result.second,
                                DMEOAuthToken(token)
                            )
                        }
                }
            }

        fun refreshCredentials() =
            SingleTransformer<Pair<DMESession, DMEOAuthToken>, ExchangeResponse> {
                it.flatMap { result ->
                    val jwt = RefreshCredentialsRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        result.second.refreshToken
                    )
                    val signingKey =
                        DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()
                    apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                        .map { ExchangeResponse(result.first, null, result.second) }
                }
            }

        // Defined above are a number of 'modules' that are used within the Cyclic Postbox flow.
        // These can be combined in various ways as the auth state demands.
        // See the flow below for details.
        var activeCredentials = credentials
        var activePostbox = existingPostbox
        val request = DMESessionRequest(
            configuration.appId,
            configuration.contractId,
            DMESDKAgent(),
            "gzip",
            null
        )

        // First, we get a session as normal.
        requestSession(request)
            // Next, we check if any credentials were supplied (for access restoration) as well as postbox.
            // If not, we kick the user out to digi.me to authorise normally which will create postbox and new set of credentials.
            .let { session ->
                if (activeCredentials != null && activePostbox != null)
                    session.map {
                        DMELog.i("CREDENTIALS NOT NULL: $it - $activePostbox - $activeCredentials")
                        Pair(it, activeCredentials!!)
                        ExchangeResponse(it, activePostbox, activeCredentials)
                    }
                else
                    session.compose(requestPreAuthorizationCode())
                        .compose(requestConsent(fromActivity))
                        .compose(exchangeAuthorizationCode())
                        .doOnSuccess {
                            DMELog.i("Item: $it")
                            activePostbox = it.postbox
                            activeCredentials = it.authToken
                        }
            }
            // At this point, we have a session, postbox and a set of credentials
            .onErrorResumeNext { error: Throwable ->

                // If an error is encountered from this call, we inspect it to see if it's an 'InternalServerError'
                // error, meaning that implicit sync was triggered wor a removed deviceId (library changed).
                // We process the consent flow for ongoing access
                when {
                    error is DMEAPIError && error.code == "InternalServerError" -> {
                        Single.just(postboxOngoingManager.sessionManager.currentSession!!)
                            .compose(requestPreAuthorizationCode())
                            .compose(requestConsent(fromActivity))
                            .compose(exchangeAuthorizationCode())
                            .doOnSuccess {
                                activePostbox = it.postbox
                                activeCredentials = it.authToken
                            }
                    }
                    // If an error we encounter is "InvalidToken" error, which means that the ACCESS token
                    // has expired.
                    error is DMEAPIError && error.code == "InvalidToken" -> {
                        // If so, we take the active session and expired credentials and try to refresh them.
                        Single.just(
                            Pair(
                                postboxOngoingManager.sessionManager.currentSession!!,
                                activeCredentials!!
                            )
                        )
                            .compose(refreshCredentials())
                            .doOnSuccess { activeCredentials = it.authToken }
                            .onErrorResumeNext { error ->

                                // If an error is encountered from this call, we inspect it to see if it's an
                                // 'InvalidToken' error, meaning that the REFRESH token has expired.
                                if (error is DMEAPIError && error.code == "InvalidToken") {
                                    // If so, we need to obtain a new set of credentials from the digi.me
                                    // application. Process the flow as before, for ongoing access, provided
                                    // that auto-recover is enabled. If not, we throw a specific error and
                                    // exit the flow.
                                    if (configuration.autoRecoverExpiredCredentials)
                                        Single.just(postboxOngoingManager.sessionManager.currentSession!!)
                                            .compose(requestPreAuthorizationCode())
                                            .compose(requestConsent(fromActivity))
                                            .compose(exchangeAuthorizationCode())
                                            .doOnSuccess {
                                                activePostbox = it.postbox
                                                activeCredentials = it.authToken
                                            } else Single.error(DMEAuthError.TokenExpired())
                                } else Single.error(error)
                            }
                    }
                    else -> Single.error(error)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: ExchangeResponse ->
                    completion(
                        result.postbox,
                        result.authToken,
                        null
                    )
                },
                onError = { error: Throwable ->
                    completion(null, null, error.let { it as? DMEError }
                        ?: DMEAPIError.GENERIC(0, error.localizedMessage ?: "Unknown"))
                }
            )
            .addTo(compositeDisposable)
    }

    fun pushDataToPostbox(postboxFile: DMEPushPayload, completion: DMEPostboxPushCompletion) {
        DMELog.i("Initializing push data to postbox.")

        if (sessionManager.isSessionValid()) {
            val encryptedData = DMEDataEncryptor.encryptedDataFromBytes(
                postboxFile.dmePostbox.publicKey,
                postboxFile.content,
                postboxFile.metadata
            )

            val multipartBody = DMEMultipartBody.Builder()
                .postboxPushPayload(postboxFile)
                .dataContent(encryptedData.fileContent, postboxFile.mimeType)
                .build()

            apiClient.makeCall(
                apiClient.argonService.pushData(
                    postboxFile.dmePostbox.sessionKey,
                    encryptedData.symmetricalKey,
                    encryptedData.iv,
                    encryptedData.metadata,
                    postboxFile.dmePostbox.postboxId,
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
        authToken: DMEOAuthToken?,
        completion: DMEOngoingPostboxPushCompletion
    ) {

        val postboxFile = postboxFile!!
        val authToken = authToken!!

        if (sessionManager.isSessionValid()) {
            val encryptedData = DMEDataEncryptor.encryptedDataFromBytes(
                postboxFile.dmePostbox.publicKey,
                postboxFile.content,
                postboxFile.metadata
            )

            val multipartBody = DMEMultipartBody.Builder()
                .postboxPushPayload(postboxFile)
                .dataContent(encryptedData.fileContent, postboxFile.mimeType)
                .build()

            val jwt = DMEAuthTokenRequestJWT(
                authToken.accessToken,
                encryptedData.iv,
                encryptedData.metadata,
                encryptedData.symmetricalKey,
                postboxFile.dmePostbox.sessionKey,
                configuration.appId,
                configuration.contractId
            )

            val signingKey = DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient.argonService.pushOngoingData(
                    authHeader,
                    postboxFile.dmePostbox.sessionKey,
                    encryptedData.symmetricalKey,
                    encryptedData.iv,
                    encryptedData.metadata,
                    postboxFile.dmePostbox.postboxId,
                    multipartBody.requestBody,
                    multipartBody.description
                )
            ) { _, error ->

                error?.let {
                    DMELog.e("Failed to push file to postbox. Error: ${error.message}")
                    completion(false, error)
                } ?: completion(true, null).also { DMELog.i("Successfully pushed data to postbox") }
            }
        }
    }

    data class ExchangeResponse(
        val session: DMESession? = null,
        val postbox: DMEPostbox? = null,
        val authToken: DMEOAuthToken? = null
    )
}

