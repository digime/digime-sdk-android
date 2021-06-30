package me.digi.ongoingpostbox.framework.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.*

fun DMEPushClient.authorizeOngoingPostbox(
    activity: Activity,
    existingPostbox: DMEPostbox? = null,
    credentials: DMEOAuthToken? = null
): Single<Pair<DMEPostbox?, DMEOAuthToken?>> =
    Single.create<Pair<DMEPostbox?, DMEOAuthToken?>> { emitter ->
        authorizeOngoingPostbox(
            activity,
            existingPostbox,
            credentials
        ) { postbox, credentials, error ->
            error?.let { emitter.onError(it) }
                ?: if (credentials != null || postbox != null)
                    emitter.onSuccess(Pair(postbox, credentials))
                else emitter.onError(DMEAuthError.General())
        }
    }

fun DMEPushClient.authorizeSaasPostbox(
    activity: Activity,
    existingPostboxData: DMEOngoingPostboxData? = null,
    credentials: DMETokenExchange? = null
): Single<DMESaasOngoingPostbox> = Single.create { emitter ->
    authorizeOngoingSaasAccess(
        activity,
        existingPostboxData,
        credentials
    ) { result, error ->
        error?.let { emitter.onError(it) }
            ?: if (result?.postboxData != null || result?.authToken != null)
                emitter.onSuccess(result)
            else emitter.onError(DMEAuthError.General())
    }
}

fun DMEPushClient.pushData(
    payload: SaasPushPayload? = null,
    credentials: DMETokenExchange? = null
): Single<SaasOngoingPushResponse> = Single.create { emitter ->
    pushDataToOngoingPostbox(payload, credentials) { response, error ->
        error?.let { emitter.onError(it) }
            ?: emitter.onSuccess(response)
    }
}