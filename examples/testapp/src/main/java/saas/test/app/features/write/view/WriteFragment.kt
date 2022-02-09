package saas.test.app.features.write.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import saas.test.app.R
import saas.test.app.data.localaccess.MainLocalDataAccess
import saas.test.app.databinding.FragmentWriteBinding
import saas.test.app.features.write.viewmodel.WriteViewModel
import saas.test.app.utils.Resource
import saas.test.app.utils.getFileContent
import saas.test.app.utils.snackBar
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.ConsentAuthResponse
import me.digi.sdk.entities.response.DataWriteResponse
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class WriteFragment : Fragment(R.layout.fragment_write), View.OnClickListener {

    private val viewModel: WriteViewModel by viewModel()
    private val binding: FragmentWriteBinding by viewBinding()
    private val localAccess: MainLocalDataAccess by inject()

    private var payloadWriteImage: WriteDataPayload? = null
    private var payloadWrite9MBImage: WriteDataPayload? = null
    private var payloadWritePdf: WriteDataPayload? = null
    private var payloadWrite9MBPdf: WriteDataPayload? = null
    private var payloadWriteJson: WriteDataPayload? = null
    private var payloadWrite8MBJson: WriteDataPayload? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handlePayload()
        subscribeToObservers()
        setupClickListeners()
    }

    private fun handlePayload() {

        val session: Session = localAccess.getCachedSession()!!
        val postboxData: ConsentAuthResponse = localAccess.getCachedPostbox()!!

        var fileContent: ByteArray = getFileContent(requireActivity(), "file.png")
        var metadata: ByteArray = getFileContent(requireActivity(), "metadatapng.json")
        var postbox: Data = Data().copy(
            key = session.key,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWriteImage = WriteDataPayload(postbox, metadata, fileContent, MimeType.IMAGE_PNG)

        fileContent = getFileContent(requireActivity(), "file9MB.png")
        metadata= getFileContent(requireActivity(), "metadata9MBpng.json")
        postbox = Data().copy(
            key = session.key,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWrite9MBImage = WriteDataPayload(postbox, metadata, fileContent, MimeType.IMAGE_PNG)

        fileContent = getFileContent(requireActivity(), "file9MB.pdf")
        metadata = getFileContent(requireActivity(), "metadata9MBpdf.json")
        postbox = Data().copy(
            key = session.key,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWrite9MBPdf = WriteDataPayload(postbox, metadata, fileContent, MimeType.APPLICATION_PDF)

        fileContent = getFileContent(requireActivity(), "file.pdf")
        metadata = getFileContent(requireActivity(), "metadatapdf.json")
        postbox = Data().copy(
            key = session.key,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWritePdf = WriteDataPayload(postbox, metadata, fileContent, MimeType.APPLICATION_PDF)

        fileContent = getFileContent(requireActivity(), "file8MB.json")
        metadata = getFileContent(requireActivity(), "metadata8MBjson.json")
        postbox = Data().copy(
            key = session.key,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWrite8MBJson = WriteDataPayload(postbox, metadata, fileContent, MimeType.TEXT_JSON)

        fileContent = getFileContent(requireActivity(), "file.json")
        metadata = getFileContent(requireActivity(), "metadatajson.json")
        postbox = Data().copy(
            key = session.key,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWriteJson = WriteDataPayload(postbox, metadata, fileContent, MimeType.TEXT_JSON)
    }

    private fun setupClickListeners() {
        binding.btnPushData.setOnClickListener(this)
        binding.clear.setOnClickListener(this)
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { result: Resource<DataWriteResponse> ->
                when (result) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.pbPush.isVisible = true
                    is Resource.Success -> {
                        binding.pbPush.isVisible = false
                        Timber.d("Response: ${result.data}")
                        snackBar("Data is: ${result.data?.status} successfully")
                    }
                    is Resource.Failure -> {
                        binding.pbPush.isVisible = false
                        snackBar(result.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPushData -> {
                if (binding.writePdf.isChecked)
                    payloadWritePdf?.let {
                        viewModel.pushDataToPostbox(
                            it,
                            localAccess.getCachedCredential()?.accessToken?.value!!
                        )
                    } ?: snackBar("Payload must not be empty")

                if (binding.write9MBPdf.isChecked)
                    payloadWrite9MBPdf?.let {
                        viewModel.pushDataToPostbox(
                            it,
                            localAccess.getCachedCredential()?.accessToken?.value!!
                        )
                    } ?: snackBar("Payload must not be empty")

                if (binding.writePng.isChecked)
                    payloadWriteImage?.let {
                        viewModel.pushDataToPostbox(
                            it,
                            localAccess.getCachedCredential()?.accessToken?.value!!
                        )
                    } ?: snackBar("Payload must not be empty")

                if (binding.write9MBPng.isChecked)
                    payloadWrite9MBImage?.let {
                        viewModel.pushDataToPostbox(
                            it,
                            localAccess.getCachedCredential()?.accessToken?.value!!
                        )
                    } ?: snackBar("Payload must not be empty")

                if (binding.writeJson.isChecked)
                    payloadWriteJson?.let {
                        viewModel.pushDataToPostbox(
                            it,
                            localAccess.getCachedCredential()?.accessToken?.value!!
                        )
                    } ?: snackBar("Payload must not be empty")
                if (binding.write8MBJson.isChecked)
                    payloadWrite8MBJson?.let {
                        viewModel.pushDataToPostbox(
                            it,
                            localAccess.getCachedCredential()?.accessToken?.value!!
                        )
                    } ?: snackBar("Payload must not be empty")
            }

            R.id.clear -> {
                binding.writeJson.isChecked = false
                binding.writePdf.isChecked = false
                binding.writePng.isChecked = false
                binding.write8MBJson.isChecked = false
                binding.write9MBPdf.isChecked = false
                binding.write9MBPng.isChecked = false
            }
        }
    }
}