package me.digi.saas.features.readraw.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.digi.saas.usecases.GetRawSessionDataUseCase
import me.digi.saas.utils.Resource
import me.digi.sdk.entities.FileListItem

class ReadRawViewModel(private val getFileList: GetRawSessionDataUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<List<FileListItem>>> =
        MutableStateFlow(Resource.Loading())
    val state: StateFlow<Resource<List<FileListItem>>>
        get() = _state

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
}