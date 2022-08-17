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
import me.digi.saas.SaasApp
import me.digi.saas.databinding.FragmentAuthBinding
import me.digi.saas.features.auth.viewmodel.AuthViewModel
import me.digi.saas.features.utils.ContractType
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.*
import me.digi.sdk.entities.response.AuthorizationResponse
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AuthFragment : Fragment(R.layout.fragment_auth), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModel()
    private val binding: FragmentAuthBinding by viewBinding()
    private var contractType: String? = null
    private var scope: DataRequest? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contractType = arguments?.getString(ContractType.key, null)

        if(contractType == ContractType.pull)
            SaasApp.instance.clearData()

        setupClickListeners()
        subscribeToObservers()

        hideScopingView()

        if(contractType == ContractType.push || contractType == ContractType.readRaw)
        {
            binding.useScoping.isVisible = false
            hideScopingView()
        } else {
            binding.useScoping.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    displayScopingView()
                else
                    hideScopingView()
            }
        }
    }

    private fun displayScopingView() {
        binding.scopeExplanation.isVisible = true

        binding.separator.isVisible = true
        binding.socialDataText.isVisible = true
        binding.separator1.isVisible = true
        binding.helathDataText.isVisible = true
        binding.separator2.isVisible = true
        binding.entertainmentDataText.isVisible = true

        binding.socialData.isVisible = true
        binding.healthFitnessData.isVisible = true
        binding.entertainmentData.isVisible = true
    }

    private fun hideScopingView() {
        binding.scopeExplanation.isVisible = false

        binding.separator.isVisible = false
        binding.socialDataText.isVisible = false
        binding.separator1.isVisible = false
        binding.helathDataText.isVisible = false
        binding.separator2.isVisible = false
        binding.entertainmentDataText.isVisible = false

        binding.socialData.isVisible = false
        binding.healthFitnessData.isVisible = false
        binding.entertainmentData.isVisible = false
    }


    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { resource: Resource<AuthorizationResponse> ->
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

        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { resource: Resource<AuthorizationResponse> ->
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

    private fun handleAuthResponse(response: AuthorizationResponse?) {
        Timber.d("Contract type: $contractType - $response")
        when (contractType) {
            ContractType.pull -> {
                if (binding.useScoping.isChecked) {
                    findNavController().navigate(R.id.read)
                } else
                    findNavController().navigate(R.id.onboardFragment)
            }
            ContractType.push -> findNavController().navigate(R.id.push)
            ContractType.readRaw -> findNavController().navigate(R.id.read_raw)
            else -> throw IllegalArgumentException("Unknown or empty contract type")
        }
    }

    private fun setupClickListeners() {
        binding.authenticate.setOnClickListener(this)

        binding.facebook.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.instagram.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.garming.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.youtube.isChecked =  false
                binding.spotify.isChecked =  false
            }
        }

        binding.instagram.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.facebook.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.garming.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.youtube.isChecked =  false
                binding.spotify.isChecked =  false
            }
        }

        binding.twitter.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.instagram.isChecked =  false
                binding.facebook.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.garming.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.youtube.isChecked =  false
                binding.spotify.isChecked =  false
            }
        }

        binding.fitbit.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.instagram.isChecked =  false
                binding.facebook.isChecked =  false

                binding.garming.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.youtube.isChecked =  false
                binding.spotify.isChecked =  false
            }
        }

        binding.garming.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.instagram.isChecked =  false
                binding.facebook.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.youtube.isChecked =  false
                binding.spotify.isChecked =  false
            }
        }

        binding.googleFit.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.instagram.isChecked =  false
                binding.facebook.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.garming.isChecked =  false

                binding.youtube.isChecked =  false
                binding.spotify.isChecked =  false
            }
        }

        binding.youtube.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.instagram.isChecked =  false
                binding.facebook.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.garming.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.spotify.isChecked =  false
            }
        }

        binding.spotify.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.twitter.isChecked =  false
                binding.instagram.isChecked =  false
                binding.facebook.isChecked =  false

                binding.fitbit.isChecked =  false
                binding.garming.isChecked =  false
                binding.googleFit.isChecked =  false

                binding.youtube.isChecked =  false
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.authenticate -> contractType?.let { type ->

                if (binding.useScoping.isChecked) {
                    var serviceType = 0
                    if(binding.facebook.isChecked)
                        serviceType = 1

                    if(binding.instagram.isChecked)
                        serviceType = 420

                    if(binding.twitter.isChecked)
                        serviceType = 2

                    if(binding.fitbit.isChecked)
                        serviceType = 15

                    if(binding.googleFit.isChecked)
                        serviceType = 284

                    if(binding.garming.isChecked)
                        serviceType = 254

                    if(binding.youtube.isChecked)
                        serviceType = 281

                    if(binding.spotify.isChecked)
                        serviceType = 16

                    viewModel.authorizeAccess(
                        requireActivity(),
                        contractType = type,
                        scope = setScope(),
                        serviceId = serviceType.toString()
                    )
                } else {
                    viewModel.authorizeAccess(
                        requireActivity(),
                        contractType = type,
                        scope = null,
                        serviceId = null
                    )
                }
            }
        }
    }

    private fun setScope(): DataRequest? {
        scope = CaScope()
        val serviceGroupList: MutableList<ServiceGroup> = mutableListOf()

        if (binding.facebook.isChecked || binding.instagram.isChecked || binding.twitter.isChecked) {
            val serviceObjectTypeList: MutableList<ServiceObjectType> = mutableListOf()
            val serviceTypeList: MutableList<ServiceType> = mutableListOf()

            if (binding.media.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(1))
            if (binding.post.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(2))
            if (binding.comment.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(7))
            if (binding.likes.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(10))

            if (binding.facebook.isChecked)
                serviceTypeList.add(ServiceType(1, serviceObjectTypeList))

            if (binding.instagram.isChecked)
                serviceTypeList.add(ServiceType(40, serviceObjectTypeList))

            if (binding.twitter.isChecked)
                serviceTypeList.add(ServiceType(3, serviceObjectTypeList))

            serviceGroupList.add(ServiceGroup(1, serviceTypeList))
        }

        if (binding.fitbit.isChecked || binding.garming.isChecked || binding.googleFit.isChecked) {
            val serviceObjectTypeList: MutableList<ServiceObjectType> = mutableListOf()
            val serviceTypeList: MutableList<ServiceType> = mutableListOf()

            if (binding.mediaFitness.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(1))
            if (binding.postFitness.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(2))
            if (binding.commentFitness.isChecked)
                serviceObjectTypeList.add(ServiceObjectType(7))

            if (binding.fitbit.isChecked)
                serviceTypeList.add(ServiceType(1, serviceObjectTypeList))
            if (binding.garming.isChecked)
                serviceTypeList.add(ServiceType(4, serviceObjectTypeList))
            if (binding.googleFit.isChecked)
                serviceTypeList.add(ServiceType(3, serviceObjectTypeList))

            serviceGroupList.add(ServiceGroup(4, serviceTypeList))
        }

        if (binding.spotify.isChecked || binding.youtube.isChecked) {
            val serviceObjectTypeList: MutableList<ServiceObjectType> = mutableListOf()
            val serviceTypeList: MutableList<ServiceType> = mutableListOf()

            if (binding.spotify.isChecked)
                serviceTypeList.add(ServiceType(1, serviceObjectTypeList))
            if (binding.youtube.isChecked)
                serviceTypeList.add(ServiceType(4, serviceObjectTypeList))

            serviceGroupList.add(ServiceGroup(5, serviceTypeList))
        }

        scope!!.serviceGroups = serviceGroupList
        return scope
    }
}