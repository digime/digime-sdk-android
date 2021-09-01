package me.digi.ongoingpostbox.features.upload.viewmodel

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.digi.ongoingpostbox.OngoingPostboxApp
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.usecases.WriteDataUseCase
import me.digi.ongoingpostbox.utils.Resource
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.DataWriteResponse

class UploadDataViewModel(
    private val uploadData: WriteDataUseCase,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ViewModel() {

    private var job: Job? = null

    private val _uploadState: MutableStateFlow<Resource<DataWriteResponse>> =
        MutableStateFlow(Resource.Idle())
    val uploadState: StateFlow<Resource<DataWriteResponse>>
        get() = _uploadState

    fun pushDataToPostbox(payload: WriteDataPayload, accessToken: String) {
        _uploadState.value = Resource.Loading()

        uploadData
            .invoke(payload, accessToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result -> _uploadState.value = Resource.Success(result) },
                onError = { error -> _uploadState.value = Resource.Failure(error.localizedMessage) }
            )
            .addTo(disposable)
    }

    private fun deleteDataAndStartOver(context: Context, lifecycleScope: CoroutineScope) {
        job?.cancel()
        job = lifecycleScope.launch {
            OngoingPostboxApp.instance.clearData()
            delay(500L)
            OngoingPostboxApp.instance.triggerAppReload(context)
        }
    }

    fun showDeleteDataAndStartOverDialog(context: Context, lifecycleScope: CoroutineScope) =
        AlertDialog.Builder(context)
            .setTitle(context.getText(R.string.label_request_session_dialog_title))
            .setMessage(context.getText(R.string.label_request_session_dialog_subtitle))
            .setPositiveButton(context.getText(R.string.action_yes)) { _, _ ->
                deleteDataAndStartOver(context, lifecycleScope)
            }
            .setNegativeButton(context.getText(R.string.action_no)) { _, _ -> }
            .create()
            .show()

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }
}