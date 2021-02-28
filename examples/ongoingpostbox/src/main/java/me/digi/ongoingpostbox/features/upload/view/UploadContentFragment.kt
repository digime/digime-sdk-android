package me.digi.ongoingpostbox.features.upload.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coil.load
import coil.transform.CircleCropTransformation
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.fragment_upload_content.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.domain.OngoingPostboxResponseBody
import me.digi.ongoingpostbox.features.upload.viewmodel.UploadContentViewModel
import me.digi.ongoingpostbox.utils.*
import me.digi.sdk.entities.DMEMimeType
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushPayload
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

class UploadContentFragment : Fragment(R.layout.fragment_upload_content), View.OnClickListener {

    private val viewModel: UploadContentViewModel by viewModel()
    private lateinit var ongoingResult: OngoingPostboxResponseBody

    companion object {
        fun newInstance() = UploadContentFragment()
        fun newInstance(ongoingResult: OngoingPostboxResponseBody) = UploadContentFragment().apply {
            arguments = Bundle().apply {
                putParcelable("ongoingResult", ongoingResult)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getParcelable<OngoingPostboxResponseBody>("ongoingResult")?.let {
            ongoingResult = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleClickListeners()
        subscribeToObservers()
    }

    private fun handleClickListeners() {
        btnUploadImage?.setOnClickListener(this)
        ivImageToUpload?.setOnClickListener(this)
    }

    private fun subscribeToObservers() {
        viewModel.uploadDataStatus.observe(
            viewLifecycleOwner,
            Observer { result: Resource<Boolean> ->
                when (result) {
                    is Resource.Loading -> {
                        pbUploadContent?.isVisible = true
                        btnUploadImage?.isEnabled = false
                    }
                    is Resource.Success -> {
                        pbUploadContent?.isVisible = false
                        btnUploadImage?.isEnabled = true
                        snackBar("Image uploaded successfully")
                        Timber.d("Image upload successful: ${result.data}")
                    }
                    is Resource.Failure -> {
                        pbUploadContent?.isVisible = false
                        btnUploadImage?.isEnabled = true
                        snackBar(result.message ?: "Unknown")
                    }
                }
            })
    }

    private fun handleImageRequest(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val fileUri: Uri = data?.data!!

                ivImageToUpload?.load(fileUri) {
                    transformations(CircleCropTransformation())
                }

                //You can get File object from intent
                val file: File = ImagePicker.getFile(data)!!
                tvImageName?.text = file.nameWithoutExtension
                tvMimeType?.isVisible = true
                tvMimeType?.text = getMimeType(requireContext(), fileUri).toString()
                btnUploadImage?.isEnabled = true

                val metadata = getFileContent(requireActivity(), "metadatapng.json")
                val postbox = DMEPostbox(
                    ongoingResult.sessionKey!!,
                    ongoingResult.postboxId!!,
                    ongoingResult.publicKey!!,
                    ongoingResult.digiMeVersion
                )
                val credentials = DMEOAuthToken(
                    ongoingResult.accessToken!!,
                    ongoingResult.expiresOn!!,
                    ongoingResult.refreshToken!!,
                    ongoingResult.tokenType!!
                )
                val postboxPayload = DMEPushPayload(
                    postbox,
                    metadata,
                    readBytes(requireContext(), fileUri)!!,
                    DMEMimeType.IMAGE_PNG
                )

                btnUploadImage?.setOnClickListener {
                    viewModel.uploadDataToOngoingPostbox(postboxPayload, credentials)
                }
            }
            ImagePicker.RESULT_ERROR -> snackBar(ImagePicker.getError(data))
            else -> snackBar("Task Cancelled")
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivImageToUpload -> ImagePicker
                .with(this)
                .compress(1024)
                .crop(16f, 9f)
                .start(::handleImageRequest)
        }
    }
}