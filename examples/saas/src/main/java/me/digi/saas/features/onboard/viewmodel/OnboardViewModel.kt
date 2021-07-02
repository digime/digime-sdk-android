package me.digi.saas.features.onboard.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.digi.saas.data.clients.SaasClients
import me.digi.saas.utils.Resource
import me.digi.sdk.saas.serviceentities.Service

class OnboardViewModel(private val client: SaasClients) : ViewModel() {

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

        client.getPullClient().onboardService(activity, onboardingCode, codeValue) { _, error ->
            if (error != null) _onboardStatus.value = Resource.Failure(error.localizedMessage)
            else _onboardStatus.value = Resource.Success(true)
        }
    }

    fun fetchServicesForContract(contractId: String) {
        _servicesStatus.value = Resource.Loading()

        viewModelScope.launch {
            client.getPullClient().getServicesForContract(contractId) { services, error ->
                services?.let { _servicesStatus.value = Resource.Success(it) }
                error?.let { _servicesStatus.value = Resource.Failure(it.localizedMessage) }
            }
        }
    }
}