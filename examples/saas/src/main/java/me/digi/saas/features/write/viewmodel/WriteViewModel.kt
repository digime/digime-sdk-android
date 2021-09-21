package me.digi.saas.features.write.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.WriteDataUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.DataWriteResponse

class WriteViewModel(private val writeData: WriteDataUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<DataWriteResponse>> = MutableStateFlow(Resource.Idle())
    val state: StateFlow<Resource<DataWriteResponse>>
        get() = _state

    fun pushDataToPostbox(payloadWrite: WriteDataPayload, accessToken: String) {
        _state.value = Resource.Loading()

        writeData
            .invoke(payloadWrite, accessToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it) },
                onError = { _state.value = Resource.Failure(it.localizedMessage) }
            )
    }
}