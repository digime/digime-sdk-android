package me.digi.ongoingpostbox.framework.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import timber.log.Timber

fun DMEPushClient.authorizeOngoingPostbox(activity: Activity, existingPostbox: DMEPostbox? = null, credentials: DMEOAuthToken? = null): Single<Pair<DMEPostbox?, DMEOAuthToken?>>
        = Single.create<Pair<DMEPostbox?, DMEOAuthToken?>> { emitter ->
    createOngoingPostbox(activity, existingPostbox, credentials) { postbox, credentials, error ->
        error?.let {
            Timber.e("ERROR YO: ${it.localizedMessage}")
            emitter.onError(it)
        } ?: if (credentials != null || postbox != null) {
            Timber.d("RRRRRX: $postbox - $credentials")
            emitter.onSuccess(Pair(postbox, credentials))
        } else emitter.onError(DMEAuthError.General())
    }
}