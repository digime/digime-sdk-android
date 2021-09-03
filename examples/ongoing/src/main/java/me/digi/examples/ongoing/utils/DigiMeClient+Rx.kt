package me.digi.examples.ongoing.utils

import android.app.Activity
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.AuthError
import me.digi.sdk.Init
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.FileItem

fun Init.authorizeOngoingAccess(
    activity: Activity,
    scope: DataRequest? = null,
    credentials: CredentialsPayload? = null,
    serviceId: String? = null
): Single<AuthorizationResponse> = Single.create { emitter ->
    authorizeAccess(activity, scope, credentials, serviceId) { credentials, error ->
        error?.let(emitter::onError)
            ?: (if (credentials != null) emitter.onSuccess(credentials)
            else emitter.onError(AuthError.General()))
    }
}

fun Init.getSessionData(accessToken: String, scope: DataRequest? = null): Observable<FileItem> =
    Observable.create { emitter ->
        readAllFiles(
            scope,
            accessToken,
            { file, error -> file?.let { emitter.onNext(it) } }) { fileList, error ->
            error?.let {
                emitter.onError(it)
            } ?: run { emitter.onComplete() }
        }
    }

fun Init.updateCurrentSession(): Single<Boolean> = Single.create { emitter ->
    updateSession { isSessionUpdated, error ->
        error?.let(emitter::onError) ?: emitter.onSuccess(isSessionUpdated as @NonNull Boolean)
    }
}