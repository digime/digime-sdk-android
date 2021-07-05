package me.digi.saas.features.pull.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.databinding.FragmentPullBinding
import me.digi.saas.features.pull.adapter.PullAdapter
import me.digi.saas.features.pull.viewmodel.PullViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.DMEFileListItem
import org.koin.androidx.viewmodel.ext.android.viewModel

class PullFragment : Fragment(R.layout.fragment_pull) {

    private val viewModel: PullViewModel by viewModel()
    private val pullAdapter: PullAdapter by lazy { PullAdapter() }
    private val binding: FragmentPullBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getData()

        subscribeToObservers()
        setupAdapter()
    }

    private fun setupAdapter() {
        binding.rvPullList.adapter = pullAdapter
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { result: Resource<List<DMEFileListItem>> ->
                when(result) {
                    is Resource.Idle -> { /** Do nothing */ }
                    is Resource.Loading -> binding.pbPull.isVisible = true
                    is Resource.Success -> {
                        binding.pbPull.isVisible = false
                        val data = result.data as List<DMEFileListItem>
                        pullAdapter.submitList(data)
                    }
                    is Resource.Failure -> {
                        binding.pbPull.isVisible = false
                        snackBar(result.message ?: "Something went wrong")
                    }
                }
            }
        }
    }
}