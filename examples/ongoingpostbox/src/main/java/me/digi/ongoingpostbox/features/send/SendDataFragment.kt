package me.digi.ongoingpostbox.features.send

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.fragment_send_data.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.features.send.viewmodel.SendDataViewModel
import me.digi.ongoingpostbox.utils.getFileContent
import me.digi.ongoingpostbox.utils.getMimeType
import me.digi.ongoingpostbox.utils.readBytes
import me.digi.sdk.entities.DMEMimeType
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File


class SendDataFragment : Fragment(R.layout.fragment_send_data) {

    private val viewModel: SendDataViewModel by viewModel()

    companion object {
        fun newInstance() = SendDataFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.uploadDataToOngoingPostboxStatus.observe(
            viewLifecycleOwner,
            Observer { result: Pair<Boolean, String?> ->
                result.second?.let {
                    Timber.e("UploadDataError: $it")
                } ?: Timber.d("Data upload was successful: ${result.first}")
            })
    }

    private fun prepareDataForUpload(result: Pair<DMEPostbox?, DMEOAuthToken?>) {

        ivSelectImage?.visibility = View.VISIBLE
        tvImagePlaceholder?.visibility = View.VISIBLE
        btnUploadImage?.visibility = View.VISIBLE
        textViewPlaceHolder?.visibility = View.GONE
        sendDataProgressBar?.visibility = View.GONE

        ivSelectImage?.setOnClickListener {
            ImagePicker
                .with(this)
                .compress(1024)
                .maxResultSize(1000, 1000)
                .start { resultCode, data ->

                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            //Image Uri will not be null for RESULT_OK
                            val fileUri: Uri = data?.data!!
                            ivSelectImage?.setImageURI(fileUri)

                            //You can get File object from intent
                            val file: File = ImagePicker.getFile(data)!!
                            Timber.d(
                                """
                                File info: ${file.nameWithoutExtension}
                                NewFile: ${getMimeType(requireContext(), fileUri)}
                            """.trimIndent()
                            )

                            btnUploadImage?.isEnabled = true

                            val metadata = getFileContent(requireActivity(), "metadatapng.json")
                            val postboxPayload = DMEPushPayload(
                                result.first!!,
                                metadata,
                                readBytes(requireContext(), fileUri)!!,
                                DMEMimeType.IMAGE_PNG
                            )

                            btnUploadImage?.setOnClickListener {
                                viewModel.uploadDataToOngoingPostbox(
                                    postboxPayload,
                                    result.second!!
                                )
                            }
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {
                            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
}