package me.digi.saas.features.readraw.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.MainActivity
import me.digi.saas.R
import me.digi.saas.databinding.FragmentReadRawBinding
import me.digi.saas.features.read.adapter.ReadAdapter
import me.digi.saas.features.readraw.viewmodel.ReadRawViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.FileListItem
import me.digi.sdk.entities.response.FileItemBytes
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ReadRawFragment : Fragment(R.layout.fragment_read_raw) {

    private val binding: FragmentReadRawBinding by viewBinding()
    private val viewModel: ReadRawViewModel by viewModel()
    private val readRawAdapter: ReadAdapter by lazy { ReadAdapter() }
    private lateinit var fileId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        subscribeToObserversList()
        subscribeToObserversData()
        setupViews()
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getData()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnGoToHome.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun setupAdapter() {
        binding.rvReadRawList.adapter = readRawAdapter
    }

    private fun subscribeToObserversList() {
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

                        readRawAdapter.setOnFileItemClickListener {
                            (activity as MainActivity)
                                .obtainStoragePermissionIfRequired()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onComplete = {
                                        fileId = it.fileId
                                        viewModel.getFileBytes(it.fileId)
                                    },
                                    onError = {})
                        }

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

    private fun subscribeToObserversData() {
        lifecycleScope.launchWhenResumed {
            viewModel.stateFileBytes.collectLatest { resource: Resource<FileItemBytes> ->
                when (resource) {
                    is Resource.Idle -> {
                        /**
                         * Do nothing
                         */
                    }
                    is Resource.Loading -> {
                        /**
                         * Do nothing
                         */
                    }
                    is Resource.Success -> {
                        val fileContentBytes = resource.data?.fileContent

                        when {
                            String(fileContentBytes!!).contains("png", true) -> {
                                openFile("*/*", ".png", fileContentBytes)
                            }
                            String(fileContentBytes).contains("pdf", true) -> {
                                openFile("*/*", ".pdf", fileContentBytes)
                            }

                            else -> {
                                openFile("*/*", ".json", fileContentBytes)
                            }
                        }
                    }
                    is Resource.Failure -> {
                        Timber.e(resource.message ?: "Unknown error occurred")
                        snackBar(resource.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }

    private fun openFile(type: String, ext: String, content: ByteArray) {
        val path = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File.createTempFile("test", ext, path)

        val os = FileOutputStream(file)
        os.write(content)
        os.close()

        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "me.digi.examples.saas.saasProvider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)

        intent.setDataAndType(uri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ContextCompat.startActivity(requireContext(), intent, null)
    }
}