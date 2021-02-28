package me.digi.ongoingpostbox.features.create.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.domain.OngoingPostboxResponseBody
import me.digi.ongoingpostbox.usecases.CreatePostboxUseCase
import me.digi.ongoingpostbox.utils.Resource

class CreatePostboxViewModel(
    private val createPostbox: CreatePostboxUseCase,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ViewModel() {

    private val _createPostboxStatus: MutableLiveData<Resource<OngoingPostboxResponseBody>> =
        MutableLiveData()
    val createPostboxStatus: LiveData<Resource<OngoingPostboxResponseBody>>
        get() = _createPostboxStatus

    fun createPostbox(activity: Activity) {
        _createPostboxStatus.postValue(Resource.Loading())

        createPostbox.invoke(activity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: OngoingPostboxResponseBody ->
                    _createPostboxStatus.postValue(Resource.Success(result))
                },
                onError = {
                    _createPostboxStatus.postValue(
                        Resource.Failure(it.localizedMessage)
                    )
                }
            )
            .addTo(disposable)
    }

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }
}