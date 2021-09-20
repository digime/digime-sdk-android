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
import kotlinx.coroutines.flow.collectLatest
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.features.upload.viewmodel.UploadDataViewModel
import me.digi.ongoingpostbox.utils.*
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.ConsentAuthResponse
import me.digi.sdk.entities.response.DataWriteResponse
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

class UploadContentFragment : Fragment(R.layout.fragment_upload_content), View.OnClickListener {

    private val viewModel: UploadDataViewModel by viewModel()
    private val localAccess: MainLocalDataAccess by inject()
    private var writePayload: WriteDataPayload? = null
    private val pickImage = 100

    companion object {
        fun newInstance(): UploadContentFragment = UploadContentFragment()
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
        lifecycleScope.launchWhenResumed {
            viewModel.uploadState.collectLatest { result: Resource<DataWriteResponse> ->
                when (result) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> {
                        pbUploadContent?.isVisible = true
                        snackBarLong(getString(R.string.label_update_ongoing))
                    }
                    is Resource.Success -> {
                        pbUploadContent?.isVisible = false
                        snackBarLong(getString(R.string.label_update_successful))

                        Timber.d("Result: ${result.data}")
                    }
                    is Resource.Failure -> {
                        pbUploadContent?.isVisible = false
                        result.message?.let { snackBarIndefiniteWithAction(it) }
                    }
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
            R.id.btnUploadImage -> writePayload?.let {
                val accessToken = localAccess.getCachedCredential()?.accessToken?.value!!
                viewModel.pushDataToPostbox(it, accessToken)
            } ?: snackBarLong("Payload must not be empty!")
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
            val file = File(it.path)

            // Update UI based on the file existence
            btnUploadImage?.isEnabled = true
            tvImageName?.text = file.nameWithoutExtension
            tvMimeType?.isVisible = true
            tvMimeType?.text = getMimeType(requireContext(), data).toString()
            btnUploadImage?.isEnabled = true

            val session: Session = localAccess.getCachedSession()!!
            val postboxData: ConsentAuthResponse = localAccess.getCachedPostbox()!!

            val fileContent: ByteArray = getFileContent(requireActivity(), file.name.toString())
            val metadata: ByteArray = getFileContent(requireActivity(), "metadatapng.json")

            val postbox: Data =
                Data().copy(
                    key = session.key,
                    postboxId = postboxData.postboxId,
                    publicKey = postboxData.publicKey
                )
            writePayload = WriteDataPayload(postbox, metadata, fileContent, MimeType.IMAGE_PNG)
        } ?: snackBarLong(getString(R.string.label_image_picker_cancelled))
    }
}