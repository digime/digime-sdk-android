package saas.test.app.features.onboard.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import saas.test.app.R
import saas.test.app.data.localaccess.MainLocalDataAccess
import saas.test.app.databinding.FragmentOnboardBinding
import saas.test.app.features.onboard.adapter.ServicesAdapter
import saas.test.app.features.onboard.viewmodel.OnboardViewModel
import saas.test.app.utils.Resource
import saas.test.app.utils.snackBar
import me.digi.sdk.entities.service.Service
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class OnboardFragment : Fragment(R.layout.fragment_onboard), View.OnClickListener {

    private val viewModel: OnboardViewModel by viewModel()
    private val binding: FragmentOnboardBinding by viewBinding()
    private val localAccess: MainLocalDataAccess by inject()
    private val serviceAdapter: ServicesAdapter by lazy { ServicesAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchServicesForContract(resources.getString(R.string.readContractId))

        setupAdapter()
        subscribeToObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.skip.setOnClickListener(this)
    }

    private fun setupAdapter() {
        binding.serviceList.adapter = serviceAdapter
        serviceAdapter.setOnServiceClickListener(::onboardService)
    }

    private fun onboardService(service: Service) {
        localAccess
            .getCachedCredential()
            ?.accessToken
            ?.value
            ?.let { viewModel.onboard(requireActivity(), service.id.toString(), it) }
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.onboardStatus.collectLatest { resource: Resource<Boolean> ->
                when (resource) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.onboardProgressBar.isVisible = true
                    is Resource.Success -> {
                        binding.onboardProgressBar.isVisible = false
                        findNavController().navigate(R.id.onboardToRead)
                    }
                    is Resource.Failure -> {
                        binding.onboardProgressBar.isVisible = false
                        Timber.e("Error: ${resource.message ?: "Unknown error occurred"}")
                        snackBar(resource.message ?: "Unknown error occurred")
                    }
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.servicesStatus.collectLatest { resource: Resource<List<Service>> ->
                when (resource) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.onboardProgressBar.isVisible = true
                    is Resource.Success -> {
                        binding.onboardProgressBar.isVisible = false

                        val services = resource.data as List<Service>
                        serviceAdapter.submitList(services)
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.skip -> findNavController().navigate(R.id.onboardToRead)
        }
    }
}