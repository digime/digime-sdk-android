package me.digi.examples.ongoing.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMESession

fun DMEPullClient.authorizeOngoingAccess(activity: Activity, scope: DMEDataRequest? = null, credentials: DMEOAuthToken? = null) = Single.create<Pair<DMESession, DMEOAuthToken>> { emitter ->
    authorizeOngoingAccess(activity, scope, credentials) { session, credentials, error ->
        error?.let {
            emitter.onError(it)
        } ?: if (session != null && credentials != null) {
            emitter.onSuccess(Pair(session, credentials))
        } else {
            emitter.onError(DMEAuthError.General())
        }
    }
}

fun DMEPullClient.getSessionData() = Observable.create<DMEFile> { emitter ->
    getSessionData({ file, error ->
        file?.let { emitter.onNext(it) }
    }) { fileList, error ->
        error?.let {
            emitter.onError(it)
        } ?: run { emitter.onComplete() }
    }
}