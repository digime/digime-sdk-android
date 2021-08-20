package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DataWriteResponse

/**
 * This is our main repository (only one too!)
 *
 * Usually in this case we could combine both remote/local data access points
 * to create seamless data flow (for instance fetch/update data via remote, save it locally,
 * then use local version to display it to the user), however since one is required within the other,
 * we're only using remote access here, and at later point in the flow we save
 * data locally
 */
class DefaultMainRepository(
    private val remoteAccess: MainRemoteDataAccess,
    private val localAccess: MainLocalDataAccess
) : MainRepository {

    override fun authorizeAccess(activity: Activity): Single<AuthorizationResponse> =
        remoteAccess
            .authorizeAccess(activity)
            .compose(localAccess.cacheAuthorizationData())

    override fun writeData(
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse> = remoteAccess.writeData(payload, accessToken)
}