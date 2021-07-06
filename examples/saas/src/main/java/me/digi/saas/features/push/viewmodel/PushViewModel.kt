package me.digi.saas.features.push.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.PushDataUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.SaasOngoingPushResponse

class PushViewModel(private val pushData: PushDataUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<SaasOngoingPushResponse>> = MutableStateFlow(Resource.Idle())
    val state: StateFlow<Resource<SaasOngoingPushResponse>>
        get() = _state

    fun pushDataToPostbox(payload: DMEPushPayload, accessToken: String) {
        pushData
            .invoke(payload, accessToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it) },
                onError = { _state.value = Resource.Failure(it.localizedMessage) }
            )
    }
}