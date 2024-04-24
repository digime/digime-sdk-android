package saas.test.app.features.read.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import saas.test.app.R
import saas.test.app.databinding.FragmentReadBinding
import saas.test.app.features.read.adapter.ReadAdapter
import saas.test.app.features.read.viewmodel.ReadViewModel
import saas.test.app.utils.Resource
import saas.test.app.utils.snackBar
import me.digi.sdk.entities.FileListItem
import me.digi.sdk.entities.response.FileList
import org.koin.androidx.viewmodel.ext.android.viewModel
import saas.test.app.features.utils.ContractType

class ReadFragment : Fragment(R.layout.fragment_read), View.OnClickListener {

    private val viewModel: ReadViewModel by viewModel()
    private val readAdapter: ReadAdapter by lazy { ReadAdapter() }
    private val binding: FragmentReadBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
        setupAdapter()
        setupViews()

        viewModel.getData()
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getData()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.btnGoToOnboard.setOnClickListener(this)
        binding.btnGoToHome.setOnClickListener(this)
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
            viewModel.state.collect { result: Resource<FileList> ->
                when (result) {
                    is Resource.Idle -> {
                        /** Do nothing */
                    }
                    is Resource.Loading -> binding.pbPull.isVisible = true
                    is Resource.Success -> {
                        if(result.data?.accounts?.first()?.error == null) {
                            binding.pbPull.isVisible = false

                            val data = result.data as FileList
                            readAdapter.submitList(data.fileList)

                            binding.numOfFiles.isVisible = true
                            binding.numOfFiles.text =
                                "Number of files: " + data.fileList.size.toString() + ", Sync status: " + data.syncStatus.rawValue

                            binding.incEmptyState.root.isVisible = data.fileList.isEmpty()
                            viewModel.getAccounts()
                        } else {
                            binding.pbPull.isVisible = false
                            binding.numOfFiles.text = "Number of files: " + result.data?.fileList?.size.toString() + ", Sync status: " + result.data?.syncStatus?.rawValue +
                                    ", sync stoped due to " + result.data?.accounts?.first()?.error!!.get("code")
                        }
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
            R.id.btnGoToOnboard -> {
                val args = Bundle()
                args.putString(ContractType.key, ContractType.pull)
                findNavController().navigate(R.id.authFragment, args)
            }

            R.id.btnGoToHome -> {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }
}