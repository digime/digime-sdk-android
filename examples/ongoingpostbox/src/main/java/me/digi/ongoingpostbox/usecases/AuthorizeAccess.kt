package me.digi.ongoingpostbox.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
import me.digi.sdk.entities.response.AuthorizationResponse

interface AuthorizeAccessUseCase {
    operator fun invoke(activity: Activity): Single<AuthorizationResponse>
}

class AuthorizeAccessUseCaseImpl(private val repository: MainRepository) : AuthorizeAccessUseCase {

    override fun invoke(activity: Activity): Single<AuthorizationResponse> =
        repository.authorizeAccess(activity)
}