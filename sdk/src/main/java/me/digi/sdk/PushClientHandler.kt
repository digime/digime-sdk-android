package me.digi.sdk

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.interapp.managers.DMEOngoingPostboxConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.DMESessionManager
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.*

object PushClientHandler {

    /**
     * Defined bellow are a number of 'modules' that are used within the Cyclic Postbox flow.
     * These can be combined in various ways as the auth state demands.
     * See the flow in [DMEPushClient] [createOngoingPostbox] for details.
     */

    fun requestSession(
        request: DMESessionRequest,
        sessionManager: DMESessionManager
    ): Single<DMESession> =
        Single.create<DMESession> { emitter ->
            DMELog.i("Requesting session")
            sessionManager.getSession(request) { session, error ->
                when {
                    session != null -> emitter.onSuccess(session)
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

    fun requestPreAuthorizationCode(
        context: Context,
        configuration: DMEPushConfiguration,
        apiClient: DMEAPIClient
    ): SingleTransformer<DMESession, DMESession> =
        SingleTransformer {
            DMELog.i("Requesting PreAuthorization code")
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
                    .map { session.apply { preauthorizationCode = it.preauthorizationCode } }
            }
        }

    fun requestConsent(
        fromActivity: Activity,
        manager: DMEOngoingPostboxConsentManager
    ): SingleTransformer<DMESession, Pair<DMESession, DMEPostbox?>> =
        SingleTransformer {
            DMELog.i("Requesting consent")
            it.flatMap { result ->
                Single.create<Pair<DMESession, DMEPostbox?>> { emitter ->
                    when (Pair(DMEAppCommunicator.getSharedInstance().canOpenDMEApp(), false)) {
                        Pair(true, false) -> {
                            manager.beginOngoingPostboxAuthorization(fromActivity) { session, postbox, error ->
                                when {
                                    session != null -> emitter.onSuccess(Pair(session, postbox))
                                    error != null -> emitter.onError(error)
                                    else -> emitter.onError(IllegalArgumentException())
                                }
                            }
                        }
                        Pair(false, false) -> {
                            DMEAppCommunicator.getSharedInstance()
                                .requestInstallOfDMEApp(fromActivity) {
                                    manager.beginOngoingPostboxAuthorization(
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
                                            else -> emitter.onError(IllegalArgumentException())
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }

    fun exchangeAuthorizationCode(
        context: Context,
        configuration: DMEPushConfiguration,
        apiClient: DMEAPIClient
    ): SingleTransformer<Pair<DMESession, DMEPostbox?>, DMEOngoingPostbox> =
        SingleTransformer {
            DMELog.i("Exchanging authorization code")
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
                        DMEOngoingPostbox(
                            result.first,
                            result.second,
                            DMEOAuthToken(token)
                        )
                    }
            }
        }

    fun refreshCredentials(configuration: DMEPushConfiguration, apiClient: DMEAPIClient) =
        SingleTransformer<Pair<DMESession, DMEOAuthToken>, DMEOngoingPostbox> {
            DMELog.i("Refreshing credentials")
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
                    .map { DMEOngoingPostbox(result.first, null, result.second) }
            }
        }

    fun triggerDataQuery(configuration: DMEPushConfiguration, apiClient: DMEAPIClient) =
        SingleTransformer<DMEOngoingPostbox, DMEOngoingPostbox> {
            it.flatMap { result: DMEOngoingPostbox ->
                val jwt = DMETriggerDataQueryRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    result.session?.key!!,
                    result.authToken?.accessToken!!
                )

                val signingKey =
                    DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)

                val authHeader = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.triggerDataQuery(authHeader))
                    .map { DMEOngoingPostbox(result.session, result.postbox, result.authToken) }
            }
        }
}