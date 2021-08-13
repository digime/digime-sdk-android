package me.digi.saas.features.readraw.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.databinding.FragmentReadRawBinding
import me.digi.saas.features.read.adapter.ReadAdapter
import me.digi.saas.features.readraw.viewmodel.ReadRawViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.FileListItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ReadRawFragment : Fragment(R.layout.fragment_read_raw) {

    private val binding: FragmentReadRawBinding by viewBinding()
    private val viewModel: ReadRawViewModel by viewModel()
    private val readRawAdapter: ReadAdapter by lazy { ReadAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        subscribeToObservers()
        setupViews()
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupAdapter() {
        binding.rvReadRawList.adapter = readRawAdapter
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { result: Resource<List<FileListItem>> ->
                when (result) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.pbReadRaw.isVisible = true
                    is Resource.Success -> {
                        binding.pbReadRaw.isVisible = false

                        val data = result.data as List<FileListItem>
                        readRawAdapter.submitList(data)

                        binding.incEmptyState.root.isVisible = data.isEmpty()
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