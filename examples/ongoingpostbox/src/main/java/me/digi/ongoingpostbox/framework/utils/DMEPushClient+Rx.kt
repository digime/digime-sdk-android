package me.digi.ongoingpostbox.framework.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.SaasOngoingPushResponse

fun DMEPushClient.authorizePostbox(
    activity: Activity,
    existingPostboxData: OngoingPostboxData? = null,
    credentials: CredentialsPayload? = null
): Single<OngoingPostbox> = Single.create { emitter ->
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
    payload: DMEPushPayload,
    accessToken: String
): Single<SaasOngoingPushResponse> = Single.create { emitter ->
    pushData(payload, accessToken) { response, error ->
        error?.let { emitter.onError(it) }
            ?: emitter.onSuccess(response as SaasOngoingPushResponse)
    }
}