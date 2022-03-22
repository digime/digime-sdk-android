package saas.test.app.features.read.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import saas.test.app.usecases.GetFileListUseCase
import saas.test.app.utils.Resource
import me.digi.sdk.entities.FileListItem
import me.digi.sdk.entities.response.FileList

class ReadViewModel(private val getFileList: GetFileListUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<FileList>> =
        MutableStateFlow(Resource.Loading())
    val state: StateFlow<Resource<FileList>>
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
                onSuccess = {
                    _state.value = Resource.Success(it)
                            },
                onError = {
                    _state.value = Resource.Failure(it.localizedMessage)
                }
            )
    }
}