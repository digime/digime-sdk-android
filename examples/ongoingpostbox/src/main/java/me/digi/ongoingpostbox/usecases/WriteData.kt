package me.digi.ongoingpostbox.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
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
        repository.writeData(payload, accessToken)
}