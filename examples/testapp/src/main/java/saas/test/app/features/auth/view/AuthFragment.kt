package saas.test.app.features.auth.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import kotlinx.coroutines.flow.collectLatest
import me.digi.sdk.Init
import me.digi.sdk.entities.*
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.response.AuthorizationResponse
import org.koin.androidx.viewmodel.ext.android.viewModel
import saas.test.app.R
import saas.test.app.databinding.FragmentAuthBinding
import saas.test.app.features.auth.viewmodel.AuthViewModel
import saas.test.app.features.utils.ContractType
import saas.test.app.utils.Resource
import saas.test.app.utils.snackBar
import timber.log.Timber

class AuthFragment : Fragment(R.layout.fragment_auth), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModel()
    private val binding: FragmentAuthBinding by viewBinding()
    private var contractType: String? = null
    private var scope: DataRequest? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contractType = arguments?.getString(ContractType.key, null)

        setupClickListeners()
        subscribeToObservers()

        hideScopingView()

        if(contractType == ContractType.push || contractType == ContractType.readRaw)
        {
            binding.useScoping.isVisible = false
            hideScopingView()
        } else {
            binding.useScoping.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    displayScopingView()
                else
                    hideScopingView()
            }
        }
    }

    private fun displayScopingView() {
        binding.separator.isVisible = true
        binding.separator1.isVisible = true
        binding.separator2.isVisible = true

        binding.socialData.isVisible = true
        binding.healthFitnessData.isVisible = true
        binding.entertainmentData.isVisible = true
    }

    private fun hideScopingView() {
        binding.separator.isVisible = false
        binding.separator1.isVisible = false
        binding.separator2.isVisible = false

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
    }

    private fun handleAuthResponse(response: AuthorizationResponse?) {
        Timber.d("Contract type: $contractType - $response")
        when (contractType) {
            ContractType.pull -> findNavController().navigate(R.id.authToOnboard)
            ContractType.push -> findNavController().navigate(R.id.authToPush)
            ContractType.readRaw -> findNavController().navigate(R.id.authToReadRaw)
            else -> throw IllegalArgumentException("Unknown or empty contract type")
        }
    }

    private fun setupClickListeners() {
        binding.authenticate.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.authenticate -> contractType?.let { type ->

                if (binding.useScoping.isChecked) {
                    viewModel.authorizeAccess(
                        requireActivity(),
                        contractType = type,
                        scope = setScope(),
                        serviceId = null
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

        if (binding.social.isChecked) {
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
                serviceTypeList.add(ServiceType(4, serviceObjectTypeList))

            if (binding.twitter.isChecked)
                serviceTypeList.add(ServiceType(3, serviceObjectTypeList))

            serviceGroupList.add(ServiceGroup(1, serviceTypeList))
        }

        if (binding.healthfitness.isChecked) {
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

        if (binding.entertainment.isChecked) {
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