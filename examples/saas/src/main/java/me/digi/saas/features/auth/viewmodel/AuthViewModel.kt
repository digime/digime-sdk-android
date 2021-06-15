package me.digi.saas.features.auth.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.utils.Resource
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.AuthSession

private const val TAG = "AuthViewModel"

class AuthViewModel(private val client: DMEPullClient) : ViewModel() {

    private val _authStatus: MutableStateFlow<Resource<AuthSession>> =
        MutableStateFlow(Resource.Idle())
    val authStatus: StateFlow<Resource<AuthSession>>
        get() = _authStatus

    fun authenticate(activity: Activity) {
        _authStatus.value = Resource.Loading()

        client.authorize(activity, null) { authSession, error ->

            authSession?.let { _authStatus.value = Resource.Success(it) }

            error?.let { _authStatus.value = Resource.Failure(it.localizedMessage) }
        }
    }
}