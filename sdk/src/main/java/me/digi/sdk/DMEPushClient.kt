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
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.callbacks.DMEPostboxOngoingCreationCompletion
import me.digi.sdk.callbacks.DMEPostboxPushCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEDataEncryptor
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.DMEAuthCodeExchangeRequestJWT
import me.digi.sdk.utilities.jwt.DMEPreauthorizationRequestJWT
import me.digi.sdk.utilities.jwt.DMETriggerDataQueryRequestJWT

class DMEPushClient(val context: Context, val configuration: DMEPushConfiguration) :
    DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy {
        DMEPostboxConsentManager(
            sessionManager,
            configuration.appId
        )
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

        fun requestConsent(fromActivity: Activity) = SingleTransformer<DMESession, DMEPostbox> {
            it.flatMap { session ->
                Single.create<DMEPostbox> { emitter ->
                    when (Pair(DMEAppCommunicator.getSharedInstance().canOpenDMEApp(), false)) {
                        Pair(true, false) -> postboxConsentManager.beginPostboxAuthorization(
                            fromActivity
                        ) { session, error ->
                            when {
                                error != null -> emitter.onError(error)
                                session != null -> emitter.onSuccess(session)
                                else -> emitter.onError(java.lang.IllegalArgumentException())
                            }
                        }
                        Pair(false, false) -> {
                            DMEAppCommunicator.getSharedInstance()
                                .requestInstallOfDMEApp(fromActivity) {
                                    postboxConsentManager.beginPostboxAuthorization(fromActivity) { session, error ->
                                        when {
                                            error != null -> emitter.onError(error)
                                            session != null -> emitter.onSuccess(session)
                                            else -> emitter.onError(java.lang.IllegalArgumentException())
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }

        fun exchangeAuthorizationCode() =
            SingleTransformer<DMESession, Pair<DMESession, DMEOAuthToken>> {
                it.flatMap { session ->

                    val codeVerifier =
                        session.metadata[context.getString(R.string.key_code_verifier)].toString()
                    val jwt = DMEAuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        session.authorizationCode!!,
                        codeVerifier
                    )

                    val signingKey =
                        DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { token ->
                            Pair(session, DMEOAuthToken(token))
                        }
                }
            }

        // Defined above are a number of 'modules' that are used within the Cyclic Postbox flow.
        // These can be combined in various ways as the auth state demands.
        // See the flow below for details.
        var activeCredentials = credentials
        val request: DMESessionRequest = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", null)

        // First, we get a session as normal.
        requestSession(request)

            // Next, we check if any credentials were supplied (for access restoration).
            // If not, we kick the user out to digi.me to authorise normally.
            .let { session ->
                if (activeCredentials != null) {
                    session.map { Pair(it, activeCredentials!!) }
                } else {
                    session.compose(requestPreAuthorizationCode())
                        .compose(requestConsent(fromActivity))
                        //.compose(exchangeAuthorizationCode())
                        .doOnSuccess {
                            //activeCredentials = it.second
                            DMELog.i("Item: $it")
                        }
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i("$it")
                },
                onError = {
                    DMELog.e("Error: ${it.localizedMessage ?: "Unknown"}")
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
}

