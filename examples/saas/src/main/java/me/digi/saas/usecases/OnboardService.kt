package me.digi.saas.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository

interface OnboardServiceUseCase {
    operator fun invoke(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>
}

class OnboardServiceUseCaseImpl(private val repository: MainRepository) : OnboardServiceUseCase {
    override fun invoke(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> =
        repository.onboardService(activity, serviceId, accessToken)
}