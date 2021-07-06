package me.digi.ongoingpostbox.framework.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.*

fun DMEPushClient.authorizePostbox(
    activity: Activity,
    existingPostboxData: DMEOngoingPostboxData? = null,
    credentials: DMETokenExchange? = null
): Single<DMESaasOngoingPostbox> = Single.create { emitter ->
    authorizeOngoingPostbox(
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
    payload: DMEPushPayload? = null,
    credentials: DMETokenExchange? = null
): Single<SaasOngoingPushResponse> = Single.create { emitter ->
    pushDataToOngoingPostbox(payload, credentials) { response, error ->
        error?.let { emitter.onError(it) }
            ?: emitter.onSuccess(response)
    }
}