package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.DMEPushPayload

interface PushDataUseCase {
    operator fun invoke(payload: DMEPushPayload): Single<Boolean>
}

class PushDataUseCaseImpl(private val repository: MainRepository) : PushDataUseCase {

    override fun invoke(payload: DMEPushPayload): Single<Boolean> =
        repository.pushDataToPostbox(payload)
}