package me.digi.ongoingpostbox.features.send

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.features.send.viewmodel.SendDataViewModel
import me.digi.ongoingpostbox.utils.getFileContent
import me.digi.sdk.entities.DMEMimeType
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class SendDataFragment : Fragment(R.layout.fragment_send_data) {

    private var firstExecution: Boolean = true
    private lateinit var resultData: Pair<DMEPostbox?, DMEOAuthToken?>
    private val viewModel: SendDataViewModel by viewModel()

    companion object {
        fun newInstance() = SendDataFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.createPostboxStatus.observe(
            viewLifecycleOwner,
            Observer { result: Pair<Pair<DMEPostbox?, DMEOAuthToken?>, String?> ->
                result.second?.let {
                    Timber.e("CreatePostboxError: $it")
                } ?: uploadDataToPostbox(result.first)
            })

        viewModel.uploadDataToOngoingPostboxStatus.observe(
            viewLifecycleOwner,
            Observer { result: Pair<Boolean, String?> ->
                result.second?.let {
                    Timber.e("UploadDataError: $it")
                } ?: Timber.d("Data upload was successful: ${result.first}")
            })
    }

    private fun uploadDataToPostbox(result: Pair<DMEPostbox?, DMEOAuthToken?>) {

        /**
         * Prepare data to be uploaded
         */
//        val fileContent = getFileContent(requireActivity(), "file_one_min.png")
        val fileContent = getFileContent(requireActivity(), "success.png")
        val metadata = getFileContent(requireActivity(), "metadatapng.json")
        val postboxPayload =
            DMEPushPayload(result.first!!, metadata, fileContent, DMEMimeType.IMAGE_PNG)

        viewModel.uploadDataToOngoingPostbox(postboxPayload, result.second!!)
    }

    override fun onResume() {
        super.onResume()

        if (firstExecution) {
            viewModel.createPostbox(requireActivity())
            firstExecution = false
        }
    }
}