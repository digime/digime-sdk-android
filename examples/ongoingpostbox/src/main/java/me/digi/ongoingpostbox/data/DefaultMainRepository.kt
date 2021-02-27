package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload

class DefaultMainRepository(private val remoteAccess: MainRemoteDataAccess) : MainRepository {

    override fun createPostbox(activity: Activity): Single<Pair<DMEPostbox?, DMEOAuthToken?>> =
        remoteAccess.createPostbox(activity)

    override fun uploadDataToOngoingPostbox(
        pushPayload: DMEPushPayload?,
        credentials: DMEOAuthToken?
    ): Single<Boolean> = remoteAccess.uploadDataToOngoingPostbox(pushPayload, credentials)
}