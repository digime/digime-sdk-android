package me.digi.saas.features.push.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.databinding.FragmentPushBinding
import me.digi.saas.features.push.viewmodel.PushViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.getFileContent
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.Postbox
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PushFragment : Fragment(R.layout.fragment_push), View.OnClickListener {

    private val viewModel: PushViewModel by viewModel()
    private val binding: FragmentPushBinding by viewBinding()
    private var payload: DMEPushPayload? = null
    private var accessToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postboxId = arguments?.getString("postboxId", null)
        val publicKey = arguments?.getString("publicKey", null)
        val sessionKey = arguments?.getString("sessionKey", null)
        accessToken = arguments?.getString("accessToken", null)

        println("Push data: $postboxId - $publicKey - $sessionKey")

        if (postboxId != null && publicKey != null && sessionKey != null) {
            val fileContent: ByteArray = getFileContent(requireActivity(), "file.png")
            val metadata: ByteArray = getFileContent(requireActivity(), "metadatapng.json")
            val postbox: Postbox =
                Postbox().copy(key = sessionKey, postboxId = postboxId, publicKey = publicKey)
            payload = DMEPushPayload(postbox, metadata, fileContent, MimeType.IMAGE_PNG)
        }

        subscribeToObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPushData.setOnClickListener(this)
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { result: Resource<SaasOngoingPushResponse> ->
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
            R.id.btnPushData -> payload?.let {
                viewModel.pushDataToPostbox(it, accessToken!!)
            } ?: snackBar("Payload must not be empty")
        }
    }
}