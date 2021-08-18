package me.digi.ongoingpostbox.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.OngoingWriteResponse

/**
 * Check [MainRemoteDataAccessImpl] for more information
 */
interface MainRemoteDataAccess {
    fun authorizeAccess(activity: Activity): Single<AuthorizationResponse>
    fun writeData(
        payload: DataPayload,
        accessToken: String
    ): Single<OngoingWriteResponse>
}