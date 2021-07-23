package me.digi.examples.ongoing.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMETokenExchange

fun DMEPullClient.authorizeOngoingAccess(
    activity: Activity,
    scope: DMEDataRequest? = null,
    credentials: DMETokenExchange? = null,
    serviceId: String? = null
): Single<DMETokenExchange> = Single.create { emitter ->
    authorizeOngoingAccess(activity, scope, credentials, serviceId) { credentials, error ->
        error?.let(emitter::onError)
            ?: (if (credentials != null)
                emitter.onSuccess(credentials)
            else emitter.onError(DMEAuthError.General()))
    }
}

fun DMEPullClient.getSessionData(): Observable<DMEFile> = Observable.create { emitter ->
    getSessionData({ file, error ->
        file?.let { emitter.onNext(it) }
    }) { fileList, error ->
        error?.let {
            emitter.onError(it)
        } ?: run { emitter.onComplete() }
    }
}