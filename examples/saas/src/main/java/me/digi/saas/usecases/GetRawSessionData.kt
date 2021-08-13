package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.entities.response.DMEFileList

interface GetRawSessionDataUseCase {
    fun invoke(): Single<DMEFileList>
}

class GetRawSessionDataUseCaseImpl(private val repository: MainRepository) :
    GetRawSessionDataUseCase {

    override fun invoke(): Single<DMEFileList> =
        repository.getRawFileList()
}