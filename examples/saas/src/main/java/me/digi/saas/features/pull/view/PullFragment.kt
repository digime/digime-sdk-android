package me.digi.saas.features.pull.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import me.digi.saas.R
import me.digi.saas.databinding.FragmentPullBinding
import me.digi.saas.features.pull.adapter.PullAdapter
import me.digi.saas.features.pull.viewmodel.PullViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.FileListItem
import org.koin.androidx.viewmodel.ext.android.viewModel

class PullFragment : Fragment(R.layout.fragment_pull), View.OnClickListener {

    private val viewModel: PullViewModel by viewModel()
    private val pullAdapter: PullAdapter by lazy { PullAdapter() }
    private val binding: FragmentPullBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getData()

        subscribeToObservers()
        setupAdapter()
        setupViews()
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getData()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.btnGoToOnboard.setOnClickListener(this)
    }

    private fun setupAdapter() {
        binding.rvPullList.adapter = pullAdapter
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collect { result: Resource<List<FileListItem>> ->
                when (result) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.pbPull.isVisible = true
                    is Resource.Success -> {
                        binding.pbPull.isVisible = false

                        val data = result.data as List<FileListItem>
                        pullAdapter.submitList(data)

                        binding.incEmptyState.root.isVisible = data.isEmpty()
                    }
                    is Resource.Failure -> {
                        binding.pbPull.isVisible = false
                        snackBar(result.message ?: "Something went wrong")
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnGoToOnboard -> findNavController().navigate(R.id.readToOnboard)
        }
    }
}