package me.digi.saas.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.AuthSession

interface AuthenticateUseCase {
    operator fun invoke(activity: Activity, contractType: String): Single<AuthSession>
}

class AuthenticateUseCaseImpl(private val repository: MainRepository): AuthenticateUseCase {

    override fun invoke(activity: Activity, contractType: String): Single<AuthSession> =
        repository.authenticate(activity, contractType)
}