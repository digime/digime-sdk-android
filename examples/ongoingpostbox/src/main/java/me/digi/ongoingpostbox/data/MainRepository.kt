package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.SaasOngoingPushResponse

interface MainRepository {
    fun authorizeAccess(activity: Activity): Single<AuthorizationResponse>
    fun writeData(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse>
}