package me.digi.ongoingpostbox.features.send.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.usecases.PushDataToOngoingPostboxUseCase
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPushPayload
import timber.log.Timber

class SendDataViewModel(
    private val uploadData: PushDataToOngoingPostboxUseCase,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ViewModel() {

    private val _uploadDataToOngoingPostboxStatus: MutableLiveData<Pair<Boolean, String?>> =
        MutableLiveData()
    val uploadDataToOngoingPostboxStatus: LiveData<Pair<Boolean, String?>>
        get() = _uploadDataToOngoingPostboxStatus

    fun uploadDataToOngoingPostbox(postboxPayload: DMEPushPayload, credentials: DMEOAuthToken) =
        uploadData
            .invoke(postboxPayload, credentials)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { isPushSuccessful ->
                    Timber.d("Push successful: $isPushSuccessful")
                    _uploadDataToOngoingPostboxStatus.postValue(Pair(isPushSuccessful, null))
                },
                onError = { error ->
                    Timber.e("Error: ${error.localizedMessage ?: "Unknown"}")
                    _uploadDataToOngoingPostboxStatus.postValue(
                        Pair(
                            false,
                            error.localizedMessage ?: "Unknown"
                        )
                    )
                }
            )
            .addTo(disposable)

    override fun onCleared() {
        Timber.d("ViewModelCleared")
        disposable.dispose()
        super.onCleared()
    }
}