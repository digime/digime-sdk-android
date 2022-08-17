package me.digi.saas.features.details.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.GetFileUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.response.FileItemBytes

class DetailsViewModel(private val getFile: GetFileUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<FileItemBytes>> = MutableStateFlow(Resource.Loading())
    val state: StateFlow<Resource<FileItemBytes>>
        get() = _state

    fun getFileByName(fileName: String) {
        getFile
            .invoke(fileName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it) },
                onError = {
                    _state.value = Resource.Failure(it.localizedMessage ?: "Unknown error occurred")
                }
            )
    }
}