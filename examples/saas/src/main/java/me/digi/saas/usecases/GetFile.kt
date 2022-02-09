package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileItemBytes

interface GetFileUseCase {
    operator fun invoke(fileId: String): Single<FileItemBytes>
}

class GetFileUseCaseImpl(private val repository: MainRepository) : GetFileUseCase {

    override fun invoke(fileId: String): Single<FileItemBytes> =
        repository.getFile(fileId)
}