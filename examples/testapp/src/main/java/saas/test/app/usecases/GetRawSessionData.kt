package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.response.FileList

interface GetRawSessionDataUseCase {
    fun invoke(): Single<FileList>
}

class GetRawSessionDataUseCaseImpl(private val repository: MainRepository) :
    GetRawSessionDataUseCase {

    override fun invoke(): Single<FileList> =
        repository.getRawFileList()
}