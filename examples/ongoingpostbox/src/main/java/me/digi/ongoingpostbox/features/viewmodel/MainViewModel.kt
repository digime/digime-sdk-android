package me.digi.ongoingpostbox.features.viewmodel

import android.app.Activity
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
import me.digi.ongoingpostbox.usecases.AuthorizeAccessUseCase
import me.digi.ongoingpostbox.usecases.WriteDataUseCase
import me.digi.ongoingpostbox.utils.Resource
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.OngoingWriteResponse

/**
 * Our [MainViewModel] contains 2 use cases since it's rather simple and small example
 * Each Use-Case is being handled individually in terms of both functionality and
 * corresponding Live/MutableLiveData
 *
 * Since our calls are Rx-based, ideally we'd dispose of them.
 * Simplest way is to pass disposable into the constructor and initialize it immediately
 * @see disposable
 * and dispose of all calls via convenience method provided by ViewModel class
 * @see onCleared
 */
class MainViewModel(
    private val createPostbox: AuthorizeAccessUseCase,
    private val uploadData: WriteDataUseCase,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ViewModel() {

    private var job: Job? = null

    private val _authState: MutableStateFlow<Resource<AuthorizationResponse>> =
        MutableStateFlow(Resource.Idle())
    val authState: StateFlow<Resource<AuthorizationResponse>>
        get() = _authState

    private val _uploadState: MutableStateFlow<Resource<OngoingWriteResponse>> =
        MutableStateFlow(Resource.Idle())
    val uploadState: StateFlow<Resource<OngoingWriteResponse>>
        get() = _uploadState

    fun createPostbox(activity: Activity) {
        _authState.value = Resource.Loading()

        createPostbox
            .invoke(activity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result -> _authState.value = Resource.Success(result) },
                onError = { _authState.value = Resource.Failure(it.localizedMessage) }
            )
            .addTo(disposable)
    }

    fun pushDataToPostbox(payload: DataPayload, accessToken: String) {
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