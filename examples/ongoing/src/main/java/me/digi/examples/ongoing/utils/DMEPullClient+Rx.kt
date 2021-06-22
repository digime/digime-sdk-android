package me.digi.examples.ongoing.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.Session

fun DMEPullClient.authOngoingSaasAccess(
    activity: Activity,
    scope: DMEDataRequest? = null,
    credentials: DMETokenExchange? = null
): Single<Pair<Session, DMETokenExchange>> = Single.create { emitter ->
    authorizeOngoingSaasAccess(activity, scope, credentials) { session, credentials, error ->
        error?.let(emitter::onError)
            ?: (if (session != null && credentials != null)
                emitter.onSuccess(Pair(session, credentials))
            else emitter.onError(DMEAuthError.General()))
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