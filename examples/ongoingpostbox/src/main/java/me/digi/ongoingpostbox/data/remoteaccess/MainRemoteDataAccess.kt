package me.digi.ongoingpostbox.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.SaasOngoingPushResponse

/**
 * Check [MainRemoteDataAccessImpl] for more information
 */
interface MainRemoteDataAccess {
    fun authorizeAccess(activity: Activity): Single<AuthorizationResponse>
    fun writeData(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse>
}