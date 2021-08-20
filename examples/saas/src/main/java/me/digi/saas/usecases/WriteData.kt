package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.DataWriteResponse

interface WriteDataUseCase {
    operator fun invoke(
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse>
}

class WriteDataUseCaseImpl(private val repository: MainRepository) : WriteDataUseCase {

    override fun invoke(
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse> =
        repository.pushDataToPostbox(payload, accessToken)
}