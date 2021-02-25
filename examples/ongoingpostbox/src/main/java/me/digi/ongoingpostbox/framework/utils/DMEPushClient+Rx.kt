package me.digi.ongoingpostbox.framework.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEOAuthToken
import timber.log.Timber

fun DMEPushClient.authorizeOngoingPostbox(activity: Activity, credentials: DMEOAuthToken? = null):
        Single<Pair<Any, DMEOAuthToken>> =
    Single.create<Pair<Any, DMEOAuthToken>> { emitter ->
        createOngoingPostbox(activity, credentials) { postbox, credentials, error ->
            error?.let {
                Timber.e("ERROR YO: ${it.localizedMessage}")
                emitter.onError(it)
            } ?: if (credentials != null) {
                Timber.d("RRRRRX: $postbox - $credentials")
                emitter.onSuccess(Pair(Any(), credentials))
            } else emitter.onError(DMEAuthError.General())
        }
    }