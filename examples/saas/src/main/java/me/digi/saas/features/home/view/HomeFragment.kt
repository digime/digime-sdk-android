package me.digi.saas.features.home.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.databinding.FragmentHomeBinding
import me.digi.saas.features.home.viewmodel.HomeViewModel
import me.digi.saas.features.utils.ContractType
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener {

    private val binding: FragmentHomeBinding by viewBinding()
    private val viewModel: HomeViewModel by viewModel()
    private val localAccess: MainLocalDataAccess by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        subscribeToObservers()
        setupClickListeners()
        setupViews()
    }

    private fun setupViews() {
        binding.triggerPullContract.isEnabled = localAccess.getCachedReadContract() != null
        binding.triggerPushContract.isEnabled = localAccess.getCachedPushContract() != null
        binding.triggerReadRawContract.isEnabled = localAccess.getCachedReadRawContract() != null
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { resource: Resource<Boolean> ->
                when (resource) {
                    is Resource.Idle -> {
                        /**
                         * Do nothing
                         */
                    }
                    is Resource.Loading -> binding.pbHome.isVisible = true
                    is Resource.Success -> {
                        binding.pbHome.isVisible = false
                        viewModel.deleteDataAndStartOver(requireContext(), lifecycleScope)
                    }
                    is Resource.Failure -> {
                        binding.pbHome.isVisible = false
                        snackBar(resource.message ?: "Unknown error occurred!")
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.triggerPullContract.setOnClickListener(this)
        binding.triggerPushContract.setOnClickListener(this)
        binding.triggerReadRawContract.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)
    }

    private fun navigateToAuthentication(type: String) {
        val bundle = Bundle()
        bundle.putString(ContractType.key, type)
        findNavController().navigate(R.id.homeToAuth, bundle)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.triggerPullContract -> navigateToAuthentication(ContractType.pull)
            R.id.triggerPushContract -> navigateToAuthentication(ContractType.push)
            R.id.triggerReadRawContract -> navigateToAuthentication(ContractType.readRaw)
            R.id.btnSettings -> findNavController().navigate(R.id.homeToSettings)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_main, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuDeleteLibrayr -> viewModel.showDeleteDataAndStartOverDialog(requireContext())
        }

        return super.onOptionsItemSelected(item)
    }
}