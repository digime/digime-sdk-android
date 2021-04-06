package me.digi.sdk

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
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
import me.digi.sdk.interapp.managers.DMEOngoingPostboxConsentManager
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEDataEncryptor
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.DMEAuthTokenRequestJWT

class DMEPushClient(
    val context: Context,
    val configuration: DMEPushConfiguration
) :
    DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy {
        DMEPostboxConsentManager(sessionManager, configuration.appId)
    }

    private val postboxOngoingManager: DMEOngoingPostboxConsentManager by lazy {
        DMEOngoingPostboxConsentManager(sessionManager, configuration.appId)
    }

    private val pushHandler by lazy { PushClientHandler }

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

    fun authorizeOngoingPostbox(
        fromActivity: Activity,
        existingPostbox: DMEPostbox? = null,
        credentials: DMEOAuthToken? = null,
        completion: DMEPostboxOngoingCreationCompletion
    ) {
        DMELog.i("Launching user consent request.")

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
        pushHandler.requestSession(request, sessionManager)
            // Next, we check if any credentials were supplied (for access restoration) as well as postbox.
            // If not, we kick the user out to digi.me to authorise normally which will create postbox and new set of credentials.
            .let { session ->
                if (activeCredentials != null && activePostbox != null)
                    session.map { DMEOngoingPostbox(it, activePostbox, activeCredentials) }
                else
                    session
                        .compose(
                            pushHandler.requestPreAuthorizationCode(
                                context,
                                configuration,
                                apiClient
                            )
                        )
                        .compose(pushHandler.requestConsent(fromActivity, postboxOngoingManager))
                        .compose(
                            pushHandler.exchangeAuthorizationCode(
                                context,
                                configuration,
                                apiClient
                            )
                        )
                        .doOnSuccess {
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
                            .compose(
                                pushHandler.requestPreAuthorizationCode(
                                    context,
                                    configuration,
                                    apiClient
                                )
                            )
                            .compose(
                                pushHandler.requestConsent(
                                    fromActivity,
                                    postboxOngoingManager
                                )
                            )
                            .compose(
                                pushHandler.exchangeAuthorizationCode(
                                    context,
                                    configuration,
                                    apiClient
                                )
                            )
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
                            .compose(pushHandler.refreshCredentials(configuration, apiClient))
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
                                            .compose(
                                                pushHandler.requestPreAuthorizationCode(
                                                    context,
                                                    configuration,
                                                    apiClient
                                                )
                                            )
                                            .compose(
                                                pushHandler.requestConsent(
                                                    fromActivity,
                                                    postboxOngoingManager
                                                )
                                            )
                                            .compose(
                                                pushHandler.exchangeAuthorizationCode(
                                                    context,
                                                    configuration,
                                                    apiClient
                                                )
                                            )
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
                onSuccess = { result -> completion(result.postbox, result.authToken, null) },
                onError = { error: Throwable ->
                    completion(null, null, error.let { it as? DMEError }
                        ?: DMEAPIError.GENERIC(0, error.localizedMessage ?: "Unknown"))
                }
            )
            .addTo(disposable)
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
        DMELog.i("Initializing push data to postbox.")

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
                    when {
                        error is DMEAPIError && error.code == "InvalidToken" -> completion(
                            false,
                            DMEAPIError.GENERIC(message = "Failed to push file to postbox. Access token is invalid. Request new session.")
                        )
                        else -> {
                            DMELog.e("Failed to push file to postbox. Error: ${it.printStackTrace()} ${error.message}")
                            completion(false, error)
                        }
                    }
                } ?: completion(true, null).also { DMELog.i("Successfully pushed data to postbox") }
            }
        } else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(false, DMEAuthError.InvalidSession())
        }
    }
}

