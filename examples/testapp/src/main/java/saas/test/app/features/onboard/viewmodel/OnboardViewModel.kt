package saas.test.app.features.onboard.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import saas.test.app.usecases.GetServicesForContractUseCase
import saas.test.app.usecases.OnboardServiceUseCase
import saas.test.app.utils.Resource
import me.digi.sdk.entities.service.Service

class OnboardViewModel(
    private val onboardService: OnboardServiceUseCase,
    private val getServicesForContract: GetServicesForContractUseCase
) : ViewModel() {

    private val _onboardStatus: MutableStateFlow<Resource<Boolean>> =
        MutableStateFlow(Resource.Idle())
    val onboardStatus: StateFlow<Resource<Boolean>>
        get() = _onboardStatus

    private val _servicesStatus: MutableStateFlow<Resource<List<Service>>> =
        MutableStateFlow(Resource.Idle())
    val servicesStatus: StateFlow<Resource<List<Service>>>
        get() = _servicesStatus

    fun onboard(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ) {
        _onboardStatus.value = Resource.Loading()

        onboardService
            .invoke(activity, serviceId, accessToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _onboardStatus.value = Resource.Success(true) },
                onError = { _onboardStatus.value = Resource.Failure(it.localizedMessage) }
            )
    }

    fun fetchServicesForContract(
        contractId: String
    ) {
        _servicesStatus.value = Resource.Loading()

        viewModelScope.launch {
            getServicesForContract
                .invoke(contractId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        _servicesStatus.value =
                            Resource.Success(it)
                    },
                    onError = { _servicesStatus.value = Resource.Failure(it.localizedMessage) }
                )
        }
    }
}