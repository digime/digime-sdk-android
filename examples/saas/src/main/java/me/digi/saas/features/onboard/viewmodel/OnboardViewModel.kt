package me.digi.saas.features.onboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.digi.saas.usecases.GetServicesForContractUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.service.Service

class OnboardViewModel(
    private val getServicesForContract: GetServicesForContractUseCase
) : ViewModel() {

    private val _servicesStatus: MutableStateFlow<Resource<List<Service>>> =
        MutableStateFlow(Resource.Idle())
    val servicesStatus: StateFlow<Resource<List<Service>>>
        get() = _servicesStatus

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