package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.DataWriteResponse

interface WriteDataUseCase {
    operator fun invoke(
        payloadWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse>
}

class WriteDataUseCaseImpl(private val repository: MainRepository) : WriteDataUseCase {

    override fun invoke(
        payloadWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse> =
        repository.pushDataToPostbox(payloadWrite, accessToken)
}