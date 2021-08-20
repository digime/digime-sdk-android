package me.digi.ongoingpostbox.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
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
        repository.writeData(payload, accessToken)
}