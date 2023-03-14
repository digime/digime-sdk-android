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
import saas.test.app.usecases.GetAccountsUseCase

class ReadViewModel(
    private val getFileList: GetFileListUseCase,
    private val getAccounts: GetAccountsUseCase
) : ViewModel() {

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
                        if(it.fileList.isNotEmpty()) {
                            _state.value = Resource.Success(it)
                        }
                            },
                onError = {
                    _state.value = Resource.Failure(it.localizedMessage)
                }
            )
    }

    fun getAccounts(){
        getAccounts
            .invoke()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    val aaa = 0
//                    if(it.fileList.isNotEmpty()) {
//                        _state.value = Resource.Success(it)
//                    }
                },
                onError = {
                    _state.value = Resource.Failure(it.localizedMessage)
                }
            )
    }


}