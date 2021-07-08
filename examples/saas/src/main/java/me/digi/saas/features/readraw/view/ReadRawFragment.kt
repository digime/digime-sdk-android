package me.digi.saas.features.readraw.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.databinding.FragmentPullRawBinding
import me.digi.saas.features.pull.adapter.PullAdapter
import me.digi.saas.features.readraw.viewmodel.ReadRawViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.DMEFileListItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ReadRawFragment: Fragment(R.layout.fragment_pull_raw) {

    private val binding: FragmentPullRawBinding by viewBinding()
    private val viewModel: ReadRawViewModel by viewModel()
    private val readRawAdapter: PullAdapter by lazy { PullAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getData()

        setupAdapter()
        subscribeToObservers()
    }

    private fun setupAdapter() {
        binding.rvReadRawList.adapter = readRawAdapter
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { result: Resource<List<DMEFileListItem>> ->
                when(result) {
                    is Resource.Idle -> { /** Do nothing */ }
                    is Resource.Loading -> binding.pbReadRaw.isVisible = true
                    is Resource.Success -> {
                        binding.pbReadRaw.isVisible = false
                        readRawAdapter.submitList(result.data ?: emptyList())
                    }
                    is Resource.Failure -> {
                        binding.pbReadRaw.isVisible = false

                        Timber.e(result.message ?: "Unknown error occurred")
                        snackBar(result.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }
}