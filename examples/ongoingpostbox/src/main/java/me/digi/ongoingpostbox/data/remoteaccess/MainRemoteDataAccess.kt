package me.digi.ongoingpostbox.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEOngoingPostboxData
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.DMETokenExchange

/**
 * Check [MainRemoteDataAccessImpl] for more information
 */
interface MainRemoteDataAccess {
    fun createPostbox(activity: Activity): Single<Pair<DMEOngoingPostboxData?, DMETokenExchange?>>
    fun uploadDataToOngoingPostbox(pushPayload: DMEPushPayload? = null, credentials: DMEOAuthToken? = null): Single<Boolean>
}