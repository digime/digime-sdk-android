package me.digi.ongoingpostbox.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.*

/**
 * Check [MainRemoteDataAccessImpl] for more information
 */
interface MainRemoteDataAccess {
    fun createPostbox(activity: Activity): Single<DMESaasOngoingPostbox?>
    fun pushDataToPostbox(payload: DMEPushPayload, accessToken: String): Single<SaasOngoingPushResponse>
}