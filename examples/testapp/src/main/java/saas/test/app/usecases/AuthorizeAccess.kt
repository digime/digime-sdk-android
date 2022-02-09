package saas.test.app.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.response.AuthorizationResponse

interface AuthorizeAccessUseCase {
    operator fun invoke(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        serviceId: String?
    ): Single<AuthorizationResponse>
}

class AuthorizeAccessUseCaseImpl(private val repository: MainRepository) : AuthorizeAccessUseCase {

    override fun invoke(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        serviceId: String?
    ): Single<AuthorizationResponse> =
        repository.authorizeAccess(
            activity,
            contractType,
            scope,
            serviceId
        )
}