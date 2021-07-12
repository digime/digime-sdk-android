package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository
import me.digi.sdk.saas.serviceentities.Service

interface GetServicesForContractUseCase {
    fun invoke(contractId: String): Single<List<Service>>
}

class GetServicesForContractUseCaseImpl(private val repository: MainRepository) :
    GetServicesForContractUseCase {

    override fun invoke(contractId: String): Single<List<Service>> =
        repository.getServicesForContract(contractId)
}