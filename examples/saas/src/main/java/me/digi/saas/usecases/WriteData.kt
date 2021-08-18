package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.OngoingWriteResponse

interface WriteDataUseCase {
    operator fun invoke(
        payload: DataPayload,
        accessToken: String
    ): Single<OngoingWriteResponse>
}

class WriteDataUseCaseImpl(private val repository: MainRepository) : WriteDataUseCase {

    override fun invoke(
        payload: DataPayload,
        accessToken: String
    ): Single<OngoingWriteResponse> =
        repository.pushDataToPostbox(payload, accessToken)
}