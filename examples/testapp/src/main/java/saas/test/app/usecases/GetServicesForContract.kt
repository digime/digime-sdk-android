package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.service.Service

interface GetServicesForContractUseCase {
    fun invoke(contractId: String): Single<List<Service>>
}

class GetServicesForContractUseCaseImpl(private val repository: MainRepository) :
    GetServicesForContractUseCase {

    override fun invoke(contractId: String): Single<List<Service>> =
        repository.getServicesForContract(contractId)
}