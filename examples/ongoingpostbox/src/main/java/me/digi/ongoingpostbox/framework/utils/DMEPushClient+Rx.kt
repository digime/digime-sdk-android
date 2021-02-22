package me.digi.ongoingpostbox.framework.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox

fun DMEPushClient.authorizeOngoingPostbox(activity: Activity, credentials: DMEOAuthToken? = null) =
    Single.create<Pair<DMEPostbox, DMEOAuthToken>> { emitter ->
        createOngoingPostbox(activity, credentials) { postbox, credentials, error ->
            error?.let {
                emitter.onError(it)
            } ?: if(postbox != null && credentials != null)
                emitter.onSuccess(Pair(postbox, credentials))
            else emitter.onError(DMEAuthError.General())
        }
    }