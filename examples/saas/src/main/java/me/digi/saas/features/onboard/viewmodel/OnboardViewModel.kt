package me.digi.saas.features.onboard.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.utils.Resource
import me.digi.sdk.DMEError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.AuthSession

class OnboardViewModel(private val client: DMEPullClient): ViewModel() {

    private val _onboardStatus: MutableStateFlow<Resource<DMEError>> =
        MutableStateFlow(Resource.Idle())
    val onboardStatus: StateFlow<Resource<DMEError>>
        get() = _onboardStatus

    fun onboard(activity: Activity, authSession: AuthSession) {
        _onboardStatus.value = Resource.Loading()

        client.onboard(activity, authSession) { error ->

            error?.let { _onboardStatus.value = Resource.Failure(it.localizedMessage) }
        }
    }
}