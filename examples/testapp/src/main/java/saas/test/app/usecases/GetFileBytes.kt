package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.response.FileItemBytes


interface GetFileBytesUseCase {
    operator fun invoke(fileId: String): Single<FileItemBytes>
}

class GetFileBytesUseCaseImpl(private val repository: MainRepository) : GetFileBytesUseCase {

    override fun invoke(fileId: String): Single<FileItemBytes> =
        repository.getFileBytes(fileId)
}