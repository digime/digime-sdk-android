package me.digi.ongoingpostbox.features.create.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.ongoingpostbox.usecases.AuthorizeAccessUseCase
import me.digi.ongoingpostbox.utils.Resource
import me.digi.sdk.entities.response.AuthorizationResponse

class CreatePostboxViewModel(
    private val createPostbox: AuthorizeAccessUseCase,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ViewModel() {

    private val _authState: MutableStateFlow<Resource<AuthorizationResponse>> =
        MutableStateFlow(Resource.Idle())
    val authState: StateFlow<Resource<AuthorizationResponse>>
        get() = _authState

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

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }
}