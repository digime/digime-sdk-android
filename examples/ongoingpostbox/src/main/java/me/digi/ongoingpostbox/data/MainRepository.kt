package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DataWriteResponse

interface MainRepository {
    fun authorizeAccess(activity: Activity): Single<AuthorizationResponse>
    fun writeData(
        payload: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse>

    fun updateSession(): Single<Boolean>
}