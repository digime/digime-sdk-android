package me.digi.saas.features.onboard.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.digi.saas.usecases.GetServicesForContractUseCase
import me.digi.saas.usecases.OnboardServiceUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.saas.serviceentities.Service

class OnboardViewModel(private val onboardService: OnboardServiceUseCase, private val getServicesForContract: GetServicesForContractUseCase) : ViewModel() {

    private val _onboardStatus: MutableStateFlow<Resource<Boolean>> =
        MutableStateFlow(Resource.Idle())
    val onboardStatus: StateFlow<Resource<Boolean>>
        get() = _onboardStatus

    private val _servicesStatus: MutableStateFlow<Resource<List<Service>>> =
        MutableStateFlow(Resource.Idle())
    val servicesStatus: StateFlow<Resource<List<Service>>>
        get() = _servicesStatus

    fun onboard(activity: Activity, onboardingCode: String, codeValue: String) {
        _onboardStatus.value = Resource.Loading()

        onboardService
            .invoke(activity, onboardingCode, codeValue)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _onboardStatus.value = Resource.Success(true) },
                onError = { error -> _onboardStatus.value = Resource.Failure(error.localizedMessage) }
            )
    }

    fun fetchServicesForContract(contractId: String) {
        _servicesStatus.value = Resource.Loading()

        viewModelScope.launch {
            getServicesForContract
                .invoke(contractId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { _servicesStatus.value = Resource.Success(it) },
                    onError = { _servicesStatus.value = Resource.Failure(it.localizedMessage) }
                )
        }
    }
}