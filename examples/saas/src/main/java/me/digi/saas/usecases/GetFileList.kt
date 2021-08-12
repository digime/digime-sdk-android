package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.response.DMEFileList

interface GetFileListUseCase {
    operator fun invoke(): Single<DMEFileList>
}

class GetFileListUseCaseImpl(private val repository: MainRepository) : GetFileListUseCase {
    override fun invoke(): Single<DMEFileList> = repository.getFileList()
}