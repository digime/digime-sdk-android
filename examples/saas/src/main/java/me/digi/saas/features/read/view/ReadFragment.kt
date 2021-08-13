package me.digi.saas.features.read.view

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import me.digi.saas.R
import me.digi.saas.databinding.FragmentReadBinding
import me.digi.saas.features.read.adapter.ReadAdapter
import me.digi.saas.features.read.viewmodel.ReadViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.FileListItem
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReadFragment : Fragment(R.layout.fragment_read), View.OnClickListener {

    private val viewModel: ReadViewModel by viewModel()
    private val readAdapter: ReadAdapter by lazy { ReadAdapter() }
    private val binding: FragmentReadBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.rvReadList.adapter = readAdapter

        readAdapter.setOnFileItemClickListener { fileListItem ->
            val data: Bundle = bundleOf("fileName" to fileListItem.fileId)
            findNavController().navigate(R.id.readToDetails, data)
        }
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
                        readAdapter.submitList(data)

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