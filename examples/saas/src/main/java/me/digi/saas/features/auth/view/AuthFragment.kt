package me.digi.saas.features.auth.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.databinding.FragmentAuthBinding
import me.digi.saas.features.auth.viewmodel.AuthViewModel
import me.digi.saas.features.utils.ContractType
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.AuthSession
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AuthFragment : Fragment(R.layout.fragment_auth), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModel()
    private val binding: FragmentAuthBinding by viewBinding()
    private var contractType: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contractType = arguments?.getString(ContractType.key, null)

        setupViews()
        setupClickListeners()
        subscribeToObservers()
    }

    private fun setupViews() {
        binding.tvAuthDescription.text = getString(R.string.labelAuthDisclaimer, contractType)

        when(contractType) {
            ContractType.pull -> binding.ivContractType.load(R.drawable.ic_download) { crossfade(true) }
            ContractType.push -> binding.ivContractType.load(R.drawable.ic_upload) { crossfade(true) }
            ContractType.readRaw -> binding.ivContractType.load(R.drawable.ic_rraw) { crossfade(true) }
        }
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.authStatus.collectLatest { resource: Resource<AuthSession> ->
                when (resource) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> {
                        binding.authProgressBar.isVisible = true
                        binding.authenticate.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.authProgressBar.isVisible = false
                        binding.authenticate.isEnabled = true
                        handleAuthResponse(resource.data)
                    }
                    is Resource.Failure -> {
                        binding.authProgressBar.isVisible = false
                        binding.authenticate.isEnabled = true
                        Timber.e("Error: ${resource.message ?: "Unknown error occurred"}")
                        snackBar(resource.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }

    private fun handleAuthResponse(response: AuthSession?) {
        Timber.d("Contract type: $contractType")
        when (contractType) {
            ContractType.pull -> goToOnboardingScreen(response?.code!!)
            ContractType.push -> gotToPushScreen(response)
            else -> throw IllegalArgumentException("Unknown or empty contract type")
        }
    }

    private fun gotToPushScreen(response: AuthSession?) {
        val bundle = Bundle()
        bundle.putString("postboxId", response?.postboxId)
        bundle.putString("publicKey", response?.publicKey)
        bundle.putString("sessionKey", response?.sessionKey)
        bundle.putString("accessToken", response?.accessToken)

        findNavController().navigate(R.id.authToPush, bundle)
    }

    private fun goToOnboardingScreen(code: String) {
        val bundle = Bundle()
        bundle.putString("code", code)
        findNavController().navigate(R.id.authToOnboard, bundle)
    }

    private fun setupClickListeners() {
        binding.authenticate.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.authenticate -> contractType?.let { viewModel.authenticate(requireActivity(), it) }
        }
    }
}