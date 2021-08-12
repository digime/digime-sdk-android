package me.digi.ongoingpostbox.features.upload.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import kotlinx.android.synthetic.main.fragment_upload_content.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.domain.OngoingPostboxPayload
import me.digi.ongoingpostbox.features.viewmodel.MainViewModel
import me.digi.ongoingpostbox.utils.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

class UploadContentFragment : Fragment(R.layout.fragment_upload_content), View.OnClickListener {

    private val viewModel: MainViewModel by viewModel()
    private val localAccess: MainLocalDataAccess by inject()
    private var firstExecution = true
    private val pickImage = 100

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
        viewModel.createPostboxStatus.observe(viewLifecycleOwner) { result: Resource<OngoingPostboxPayload> ->
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
        }

        viewModel.uploadDataStatus.observe(viewLifecycleOwner) { result: Resource<SaasOngoingPushResponse> ->
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

                    Timber.d("Result: ${result.data}")
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
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivImageToUpload -> {
                val gallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, pickImage)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) handleImage(data?.data)
    }

    private fun handleImage(data: Uri?) {
        data?.let {
            ivImageToUpload?.load(data) {
                crossfade(true)
                transformations(RoundedCornersTransformation(15f))
            }

            //You can get File object from intent
            val file = File(data.path)

            // Update UI based on the file existence
            btnUploadImage?.isEnabled = true
            tvImageName?.text = file.nameWithoutExtension
            tvMimeType?.isVisible = true
            tvMimeType?.text = getMimeType(requireContext(), data).toString()
            btnUploadImage?.isEnabled = true

            /**
             * At this point we have
             * @see [localAccess.getCachedPostbox]
             * @see [localAccess.getCachedCredential]
             * @see [localAccess.getCachedSession]
             * so we can say they're not null
             */
            val metadata: ByteArray = getFileContent(requireActivity(), "metadatapng.json")
            val postbox: OngoingPostboxData = localAccess.getCachedPostbox()!!
            val credentials: CredentialsPayload = localAccess.getCachedCredential()!!
            val session: Session = localAccess.getCachedSession()!!

            val saasPostbox = Postbox().copy(
                key = session.key,
                postboxId = postbox.postboxId,
                publicKey = postbox.publicKey
            )

            val postboxPayload = DMEPushPayload(
                saasPostbox,
                metadata,
                readBytes(requireContext(), data)!!,
                MimeType.IMAGE_PNG
            )

            btnUploadImage?.setOnClickListener {
                viewModel.pushDataToPostbox(postboxPayload, credentials.accessToken.value!!)
            }
        } ?: snackBarLong(getString(R.string.label_image_picker_cancelled))
    }
}