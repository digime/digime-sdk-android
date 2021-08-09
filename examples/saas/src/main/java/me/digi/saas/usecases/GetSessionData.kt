package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.response.DMEFileList

interface GetSessionDataUseCase {
    operator fun invoke(): Single<DMEFileList>
}

class GetSessionDataUseCaseImpl(private val repository: MainRepository) : GetSessionDataUseCase {
    override fun invoke(): Single<DMEFileList> = repository.getFileList()
}