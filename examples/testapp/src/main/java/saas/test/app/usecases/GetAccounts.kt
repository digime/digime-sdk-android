package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.response.FileList
import me.digi.sdk.entities.response.ReadAccountsResponse

interface GetAccountsUseCase {
    operator fun invoke(): Single<ReadAccountsResponse>
}

class GetAccountsUseCaseImpl(private val repository: MainRepository) : GetAccountsUseCase {
    override fun invoke(): Single<ReadAccountsResponse>  = repository.getAccounts()
}