package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.SaasOngoingPushResponse

interface WriteDataUseCase {
    operator fun invoke(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse>
}

class WriteDataUseCaseImpl(private val repository: MainRepository) : WriteDataUseCase {

    override fun invoke(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse> =
        repository.pushDataToPostbox(payload, accessToken)
}