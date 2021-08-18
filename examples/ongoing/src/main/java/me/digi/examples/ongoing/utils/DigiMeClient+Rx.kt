package me.digi.examples.ongoing.utils

import android.app.Activity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.AuthError
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DMEFile
import me.digi.sdk.unify.DigiMe

fun DigiMe.authorizeOngoingAccess(
    activity: Activity,
    scope: DataRequest? = null,
    credentials: CredentialsPayload? = null,
    serviceId: String? = null
): Single<AuthorizationResponse> = Single.create { emitter ->
    authorizeReadAccess(activity, scope, credentials, serviceId) { credentials, error ->
        error?.let(emitter::onError)
            ?: (if (credentials != null) emitter.onSuccess(credentials)
            else emitter.onError(AuthError.General()))
    }
}

fun DigiMe.getSessionData(): Observable<DMEFile> = Observable.create { emitter ->
    readFiles({ file, error -> file?.let { emitter.onNext(it) } }) { fileList, error ->
        error?.let {
            emitter.onError(it)
        } ?: run { emitter.onComplete() }
    }
}