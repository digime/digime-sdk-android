package me.digi.saas.features.auth.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.AuthorizeAccessUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.response.AuthorizationResponse

class AuthViewModel(private val authorizeAccess: AuthorizeAccessUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<AuthorizationResponse>> =
        MutableStateFlow(Resource.Idle())
    val state: StateFlow<Resource<AuthorizationResponse>>
        get() = _state

    fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest? = null,
        serviceId: String? = null
    ) {
        _state.value = Resource.Loading()

        authorizeAccess
            .invoke(
                activity,
                contractType,
                scope,
                serviceId
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it) },
                onError = { _state.value = Resource.Failure(it.localizedMessage) }
            )
    }
}