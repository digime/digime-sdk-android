package me.digi.saas.features.write.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.databinding.FragmentWriteBinding
import me.digi.saas.entities.LocalSession
import me.digi.saas.features.write.viewmodel.WriteViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.getFileContent
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.WriteDataInfo
import me.digi.sdk.entities.response.DataWriteResponse
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class WriteFragment : Fragment(R.layout.fragment_write), View.OnClickListener {

    private val viewModel: WriteViewModel by viewModel()
    private val binding: FragmentWriteBinding by viewBinding()
    private val localAccess: MainLocalDataAccess by inject()
    private var payloadWrite: WriteDataPayload? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handlePayload()
        subscribeToObservers()
        setupClickListeners()
    }

    private fun handlePayload() {

        val session: LocalSession = localAccess.getCachedSession()!!
        val postboxData: WriteDataInfo = localAccess.getCachedPostbox()!!

        val fileContent: ByteArray = getFileContent(requireActivity(), "file.png")
        val metadata: ByteArray = getFileContent(requireActivity(), "metadatapng.json")
        val postbox: Data = Data().copy(
            key = session.sessionKey,
            postboxId = postboxData.postboxId,
            publicKey = postboxData.publicKey
        )

        payloadWrite = WriteDataPayload(postbox, metadata, fileContent, MimeType.IMAGE_PNG)
    }

    private fun setupClickListeners() {
        binding.btnPushData.setOnClickListener(this)
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
            R.id.btnPushData -> payloadWrite?.let {
                viewModel.pushDataToPostbox(
                    it,
                    localAccess.getCachedCredential()?.accessToken?.value!!
                )
            } ?: snackBar("Payload must not be empty")
        }
    }
}