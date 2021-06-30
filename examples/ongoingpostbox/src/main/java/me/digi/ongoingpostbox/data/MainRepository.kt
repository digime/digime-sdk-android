package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.DMESaasOngoingPostbox
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.SaasOngoingPushResponse

interface MainRepository {
    fun createPostbox(activity: Activity): Single<DMESaasOngoingPostbox?>
    fun uploadDataToOngoingPostbox(pushPayload: DMEPushPayload? = null, credentials: DMETokenExchange? = null): Single<SaasOngoingPushResponse>
}