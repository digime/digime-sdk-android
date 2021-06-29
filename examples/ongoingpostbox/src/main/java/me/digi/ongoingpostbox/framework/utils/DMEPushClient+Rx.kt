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
): Single<Pair<DMEOngoingPostboxData?, DMETokenExchange?>> = Single.create { emitter ->
    authorizeOngoingSaasAccess(
        activity,
        existingPostboxData,
        credentials
    ) { postbox, credentials, error ->
        error?.let { emitter.onError(it) }
            ?: if (credentials != null || postbox != null)
                emitter.onSuccess(Pair(postbox, credentials))
            else emitter.onError(DMEAuthError.General())
    }
}

fun DMEPushClient.pushData(
    payload: DMEPushPayload? = null,
    credentials: DMEOAuthToken? = null
): Single<Boolean> = Single.create<Boolean> { emitter ->
    pushDataToOngoingPostbox(payload, credentials) { isDataPushSuccessful, error ->
        error?.let { emitter.onError(it) }
            ?: emitter.onSuccess(isDataPushSuccessful)
    }
}