package me.digi.saas.features.home.viewmodel

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.digi.saas.R
import me.digi.saas.SaasApp
import me.digi.saas.usecases.DeleteLibraryUseCase
import me.digi.saas.utils.Resource

class HomeViewModel(private val deleteLibrary: DeleteLibraryUseCase) : ViewModel() {

    private val _state: MutableStateFlow<Resource<Boolean>> = MutableStateFlow(Resource.Idle())
    val state: StateFlow<Resource<Boolean>>
        get() = _state

    private var job: Job? = null

    private fun deleteLibrary() {
        deleteLibrary
            .invoke()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { _state.value = Resource.Success(it) },
                onError = { _state.value = Resource.Failure(it.localizedMessage) }
            )
    }

    fun deleteDataAndStartOver(context: Context, lifecycleScope: CoroutineScope) {
        job?.cancel()
        job = lifecycleScope.launch {
            SaasApp.instance.clearData()
            delay(500L)
            SaasApp.instance.triggerAppReload(context)
        }
    }

    fun showDeleteDataAndStartOverDialog(context: Context) =
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.dialogDeleteLibraryTitle))
            .setMessage(context.getString(R.string.dialogDeleteLibrarySubtitle))
            .setPositiveButton(context.getString(R.string.actionYes)) { _, _ -> deleteLibrary() }
            .setNegativeButton(context.getString(R.string.actionNo)) { _, _ -> }
            .create()
            .show()
}