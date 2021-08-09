package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.DMESaasOngoingPostbox
import me.digi.sdk.entities.SaasOngoingPushResponse

/**
 * This is our main repository (only one too!)
 *
 * Usually in this case we could combine both remote/local data access points
 * to create seamless data flow (for instance fetch/update data via remote, save it locally,
 * then use local version to display it to the user), however since one is required within the other,
 * we're only using remote access here, and at later point in the flow we save
 * data locally
 */
class DefaultMainRepository(private val remoteAccess: MainRemoteDataAccess) : MainRepository {

    override fun createPostbox(activity: Activity): Single<DMESaasOngoingPostbox?> =
        remoteAccess.createPostbox(activity)

    override fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse> =
        remoteAccess.pushDataToPostbox(payload, accessToken)
}