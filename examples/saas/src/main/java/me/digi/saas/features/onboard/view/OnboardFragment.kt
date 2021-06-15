package me.digi.saas.features.onboard.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.databinding.FragmentOnboardBinding
import me.digi.saas.features.onboard.viewmodel.OnboardViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.DMEError
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class OnboardFragment : Fragment(R.layout.fragment_onboard) {

    private val viewModel: OnboardViewModel by viewModel()
    private val binding: FragmentOnboardBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.onboardStatus.collectLatest { resource: Resource<DMEError> ->
                when(resource) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.onboardProgressBar.isVisible = true
                    is Resource.Success -> {
                        binding.onboardProgressBar.isVisible = false
                    }
                    is Resource.Failure -> {
                        binding.onboardProgressBar.isVisible = false

                        Timber.e("Error: ${resource.message ?: "Unknown error occurred"}")

                        snackBar(resource.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }
}