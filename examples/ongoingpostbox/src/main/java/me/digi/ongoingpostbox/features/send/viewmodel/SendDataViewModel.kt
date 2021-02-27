package me.digi.ongoingpostbox.features.send.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.usecases.CreatePostboxUseCase
import me.digi.ongoingpostbox.usecases.PushDataToOngoingPostboxUseCase
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload
import timber.log.Timber

class SendDataViewModel(
    private val createPostbox: CreatePostboxUseCase,
    private val uploadDataToOngoingPostbox: PushDataToOngoingPostboxUseCase
) : ViewModel() {

    private val _createPostboxStatus: MutableLiveData<Pair<Pair<DMEPostbox?, DMEOAuthToken?>, String?>> =
        MutableLiveData()
    val createPostboxStatus: LiveData<Pair<Pair<DMEPostbox?, DMEOAuthToken?>, String?>>
        get() = _createPostboxStatus

    private val _uploadDataToOngoingPostboxStatus: MutableLiveData<Pair<Boolean, String?>> =
        MutableLiveData()
    val uploadDataToOngoingPostboxStatus: LiveData<Pair<Boolean, String?>>
        get() = _uploadDataToOngoingPostboxStatus

    private val compositeDisposable = CompositeDisposable()

    fun createPostbox(activity: Activity) = createPostbox.invoke(activity)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onSuccess = { result: Pair<DMEPostbox?, DMEOAuthToken?> ->
                Timber.d("Data: ${result.first} - ${result.second}")
                _createPostboxStatus.postValue(Pair(result, null))
            },
            onError = {
                Timber.e("ErrorOccurredClient: ${it.localizedMessage ?: "Unknown"}")
                _createPostboxStatus.postValue(
                    Pair(
                        Pair(null, null),
                        it.localizedMessage ?: "Unknown"
                    )
                )
            }
        )
        .addTo(compositeDisposable)

    fun uploadDataToOngoingPostbox(postboxPayload: DMEPushPayload, credentials: DMEOAuthToken) =
        uploadDataToOngoingPostbox
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
            .addTo(compositeDisposable)

    override fun onCleared() {
        Timber.d("ViewModelCleared")
        compositeDisposable.dispose()
        super.onCleared()
    }
}