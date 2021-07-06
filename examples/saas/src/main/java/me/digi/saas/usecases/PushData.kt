package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.SaasOngoingPushResponse

interface PushDataUseCase {
    operator fun invoke(payload: DMEPushPayload, credentials: DMETokenExchange): Single<SaasOngoingPushResponse>
}

class PushDataUseCaseImpl(private val repository: MainRepository) : PushDataUseCase {

    override fun invoke(payload: DMEPushPayload, credentials: DMETokenExchange): Single<SaasOngoingPushResponse> =
        repository.pushDataToPostbox(payload, credentials)
}