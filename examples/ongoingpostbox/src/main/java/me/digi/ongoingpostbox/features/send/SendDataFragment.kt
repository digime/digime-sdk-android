package me.digi.ongoingpostbox.features.send

import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_send_data.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.framework.datasource.DigiMeService
import me.digi.ongoingpostbox.utils.getFileContent
import me.digi.sdk.entities.DMEMimeType
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload
import org.koin.android.ext.android.inject
import timber.log.Timber

class SendDataFragment : Fragment(R.layout.fragment_send_data) {

    private val service: DigiMeService by inject()
    private var firstExecution: Boolean = true
    private lateinit var resultData: Pair<DMEPostbox?, DMEOAuthToken?>

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
                onSuccess = { result: Pair<DMEPostbox?, DMEOAuthToken?> ->
                    Timber.d("Data: ${result.first} - ${result.second}")
                    textViewPlaceHolder?.text = result.toString()

                    resultData = result

                    /**
                     * Prepare data to be sent to Postbox
                     */
                    val fileContent = getFileContent(requireActivity(), "file_one_min.png")
                    val metadata = getFileContent(requireActivity(), "metadatapng.json")
                    val postboxPayload =
                        DMEPushPayload(result.first!!, metadata, fileContent, DMEMimeType.IMAGE_PNG)

                    service.pushDataToOngoingPostbox(postboxPayload, result.second!!)
                },
                onError = {
                    Timber.e("ErrorOccurredClient: ${it.localizedMessage ?: "Unknown"}")
                }
            )
    }
}