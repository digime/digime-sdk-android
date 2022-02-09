package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.response.FileList

interface GetFileListUseCase {
    operator fun invoke(): Single<FileList>
}

class GetFileListUseCaseImpl(private val repository: MainRepository) : GetFileListUseCase {
    override fun invoke(): Single<FileList> = repository.getFileList()
}