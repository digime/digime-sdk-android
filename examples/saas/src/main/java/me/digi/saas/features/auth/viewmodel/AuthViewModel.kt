package me.digi.saas.features.auth.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.AuthenticateUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.AuthSession

private const val TAG = "AuthViewModel"

class AuthViewModel(private val authenticate: AuthenticateUseCase) : ViewModel() {

    private val _authStatus: MutableStateFlow<Resource<AuthSession>> =
        MutableStateFlow(Resource.Idle())
    val authStatus: StateFlow<Resource<AuthSession>>
        get() = _authStatus

    fun authenticate(activity: Activity, contractType: String) {
        _authStatus.value = Resource.Loading()

        authenticate
            .invoke(activity, contractType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _authStatus.value = Resource.Success(it) },
                onError = { _authStatus.value = Resource.Failure(it.localizedMessage) }
            )
    }
}