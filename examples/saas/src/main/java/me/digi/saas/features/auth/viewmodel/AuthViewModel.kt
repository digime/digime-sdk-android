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
import me.digi.sdk.entities.AuthorizeResponse

class AuthViewModel(private val authenticate: AuthenticateUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<AuthorizeResponse>> =
        MutableStateFlow(Resource.Idle())
    val state: StateFlow<Resource<AuthorizeResponse>>
        get() = _state

    fun authenticate(activity: Activity, contractType: String) {
        _state.value = Resource.Loading()

        authenticate
            .invoke(activity, contractType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it) },
                onError = { _state.value = Resource.Failure(it.localizedMessage) }
            )
    }
}