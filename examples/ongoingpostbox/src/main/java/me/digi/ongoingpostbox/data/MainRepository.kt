package me.digi.ongoingpostbox.data

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload

interface MainRepository {
    fun createPostbox(activity: Activity): Single<Pair<DMEPostbox?, DMEOAuthToken?>>
    fun uploadDataToOngoingPostbox(pushPayload: DMEPushPayload? = null, credentials: DMEOAuthToken? = null): Single<Boolean>
}