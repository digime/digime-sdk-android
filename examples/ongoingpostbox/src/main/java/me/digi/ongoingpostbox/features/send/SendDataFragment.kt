package me.digi.ongoingpostbox.features.send

import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.framework.datasource.DigiMeService
import org.koin.android.ext.android.inject
import timber.log.Timber

class SendDataFragment : Fragment(R.layout.fragment_send_data) {

    private val service: DigiMeService by inject()
    private var firstExecution: Boolean = true

    companion object {
        fun newInstance() = SendDataFragment()
    }

    override fun onResume() {
        super.onResume()

        if (firstExecution) {
            handleFlow()
            firstExecution = false
        }
    }

    private fun handleFlow() {
        service.obtainAccessRights(requireActivity())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {

                },
                onError = {
                    Timber.e("Error: ${it.localizedMessage ?: "Unknown"}")
                }
            )
    }
}