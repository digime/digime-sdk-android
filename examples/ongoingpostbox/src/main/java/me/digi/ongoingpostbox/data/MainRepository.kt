package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.OngoingPostbox
import me.digi.sdk.entities.response.SaasOngoingPushResponse

interface MainRepository {
    fun createPostbox(activity: Activity): Single<OngoingPostbox?>
    fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse>
}