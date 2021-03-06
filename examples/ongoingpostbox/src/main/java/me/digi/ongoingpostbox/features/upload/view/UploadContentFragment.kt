package me.digi.ongoingpostbox.features.upload.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.fragment_upload_content.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.domain.OngoingPostboxResponseBody
import me.digi.ongoingpostbox.features.viewmodel.MainViewModel
import me.digi.ongoingpostbox.utils.*
import me.digi.sdk.entities.DMEMimeType
import me.digi.sdk.entities.DMEPushPayload
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class UploadContentFragment : Fragment(R.layout.fragment_upload_content), View.OnClickListener {

    private val viewModel: MainViewModel by viewModel()
    private val localAccess: MainLocalDataAccess by inject()
    private var firstExecution = true

    companion object {
        fun newInstance() = UploadContentFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        handleClickListeners()
        subscribeToObservers()
    }

    private fun handleClickListeners() {
        btnUploadImage?.setOnClickListener(this)
        ivImageToUpload?.setOnClickListener(this)
    }

    private fun subscribeToObservers() {
        viewModel.createPostboxStatus.observe(
            viewLifecycleOwner,
            Observer { result: Resource<OngoingPostboxResponseBody> ->
                when (result) {
                    is Resource.Loading -> {
                        pbUploadContent?.isVisible = true
                        ivImageToUpload?.isClickable = false
                        snackBarLong(getString(R.string.label_postbox_creation_started))
                    }
                    is Resource.Success -> {
                        pbUploadContent?.isVisible = false
                        ivImageToUpload?.isClickable = true
                        snackBarLong(getString(R.string.label_postbox_created))
                    }
                    is Resource.Failure -> {
                        pbUploadContent?.isVisible = false
                        ivImageToUpload?.isClickable = true
                        snackBarLong(result.message ?: getString(R.string.label_unknown_error))
                    }
                }
            })

        viewModel.uploadDataStatus.observe(
            viewLifecycleOwner,
            Observer { result: Resource<Boolean> ->
                when (result) {
                    is Resource.Loading -> {
                        pbUploadContent?.isVisible = true
                        btnUploadImage?.isEnabled = false
                        ivImageToUpload?.isClickable = false
                        snackBarLong(getString(R.string.label_update_ongoing))
                    }
                    is Resource.Success -> {
                        pbUploadContent?.isVisible = false
                        btnUploadImage?.isEnabled = true
                        ivImageToUpload?.isClickable = true
                        snackBarLong(getString(R.string.label_update_successful))
                    }
                    is Resource.Failure -> {
                        pbUploadContent?.isVisible = false
                        btnUploadImage?.isEnabled = true
                        ivImageToUpload?.isClickable = true
                        snackBarIndefiniteWithAction(
                            result.message ?: getString(R.string.label_unknown_error)
                        )
                    }
                }
            })
    }

    private fun handleImageRequest(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val fileUri: Uri = data?.data!!

                ivImageToUpload?.load(fileUri) {
                    transformations(RoundedCornersTransformation(15f))
                }

                //You can get File object from intent
                val file: File = ImagePicker.getFile(data)!!

                // Update UI based on the file existence
                btnUploadImage?.isEnabled = true
                tvImageName?.text = file.nameWithoutExtension
                tvMimeType?.isVisible = true
                tvMimeType?.text = getMimeType(requireContext(), fileUri).toString()
                btnUploadImage?.isEnabled = true

                /**
                 * At this point we have both
                 * @see [localAccess.getCachedPostbox]
                 * @see [localAccess.getCachedCredential]
                 * so we can say they're not null
                 */
                val metadata = getFileContent(requireActivity(), "metadatapng.json")
                val postbox = localAccess.getCachedPostbox()!!
                val credentials = localAccess.getCachedCredential()!!
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
            ImagePicker.RESULT_ERROR -> snackBarLong(ImagePicker.getError(data))
            else -> snackBarLong(getString(R.string.label_image_picker_cancelled))
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

    override fun onResume() {
        super.onResume()
        if (firstExecution) {
            viewModel.createPostbox(requireActivity())
            firstExecution = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_main, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_request_session -> viewModel.showDeleteDataAndStartOverDialog(
                requireContext(),
                lifecycleScope
            )
        }

        return super.onOptionsItemSelected(item)
    }
}