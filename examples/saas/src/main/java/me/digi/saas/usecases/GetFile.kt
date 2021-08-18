package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.response.File

interface GetFileUseCase {
    operator fun invoke(fileId: String): Single<File>
}

class GetFileUseCaseImpl(private val repository: MainRepository) : GetFileUseCase {

    override fun invoke(fileId: String): Single<File> =
        repository.getFile(fileId)
}