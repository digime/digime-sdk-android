package me.digi.ongoingpostbox.features.upload.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.usecases.PushDataToOngoingPostboxUseCase
import me.digi.ongoingpostbox.utils.Resource
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPushPayload
import timber.log.Timber

class UploadContentViewModel(
    private val uploadData: PushDataToOngoingPostboxUseCase,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ViewModel() {

    private val _uploadDataStatus: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val uploadDataStatus: LiveData<Resource<Boolean>>
        get() = _uploadDataStatus

    fun uploadDataToOngoingPostbox(postboxPayload: DMEPushPayload, credentials: DMEOAuthToken) {
        _uploadDataStatus.postValue(Resource.Loading())

        uploadData
            .invoke(postboxPayload, credentials)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { isPushSuccessful ->
                    _uploadDataStatus.postValue(Resource.Success(isPushSuccessful))
                },
                onError = { error ->
                    _uploadDataStatus.postValue(Resource.Failure(error.localizedMessage))
                }
            )
            .addTo(disposable)
    }

    override fun onCleared() {
        Timber.d("ViewModelCleared")
        disposable.dispose()
        super.onCleared()
    }
}