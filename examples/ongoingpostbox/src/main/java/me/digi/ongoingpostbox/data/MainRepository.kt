package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DMESaasOngoingPostbox
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.SaasOngoingPushResponse
import me.digi.sdk.entities.SaasPushPayload

interface MainRepository {
    fun createPostbox(activity: Activity): Single<DMESaasOngoingPostbox?>
    fun uploadDataToOngoingPostbox(pushPayload: SaasPushPayload? = null, credentials: DMETokenExchange? = null): Single<SaasOngoingPushResponse>
}