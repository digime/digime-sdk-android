package me.digi.saas.features.auth.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
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

        setupClickListeners()
        subscribeToObservers()
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
        when(contractType) {
            ContractType.pull -> goToOnboardingScreen(response?.code!!)
            ContractType.push -> gotToPushScreen()
            else -> throw IllegalArgumentException("Unknown or empty contract type")
        }
    }

    private fun gotToPushScreen() {
        findNavController().navigate(R.id.authToPush)
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