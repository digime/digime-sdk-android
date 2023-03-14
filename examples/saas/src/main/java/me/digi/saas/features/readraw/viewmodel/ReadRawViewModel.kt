package me.digi.saas.features.readraw.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.GetFileBytesUseCase
import me.digi.saas.usecases.GetRawSessionDataUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.FileListItem
import me.digi.sdk.entities.response.FileItemBytes

class ReadRawViewModel(private val getFileList: GetRawSessionDataUseCase, private val getFileBytes: GetFileBytesUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<List<FileListItem>>> =
        MutableStateFlow(Resource.Loading())
    val state: StateFlow<Resource<List<FileListItem>>>
        get() = _state

    private val _stateFileBytes: MutableStateFlow<Resource<FileItemBytes>> = MutableStateFlow(Resource.Loading())
    val stateFileBytes: StateFlow<Resource<FileItemBytes>>
        get() = _stateFileBytes

    init {
        getData()
    }

    fun getData() {
        getFileList
            .invoke()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it.fileList) },
                onError = { _state.value = Resource.Failure(it.localizedMessage) }
            )
    }

    fun getFileBytes(fileName: String) {
        getFileBytes
            .invoke(fileName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _stateFileBytes.value = Resource.Success(it) },
                onError = {
                    _stateFileBytes.value = Resource.Failure(it.localizedMessage ?: "Unknown error occurred")
                }
            )
    }
}