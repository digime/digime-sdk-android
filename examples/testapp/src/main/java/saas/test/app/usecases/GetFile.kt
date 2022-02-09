package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileItemBytes

interface GetFileUseCase {
    operator fun invoke(fileId: String): Single<FileItemBytes>
}

class GetFileUseCaseImpl(private val repository: MainRepository) : GetFileUseCase {

    override fun invoke(fileId: String): Single<FileItemBytes> =
        repository.getFile(fileId)
}