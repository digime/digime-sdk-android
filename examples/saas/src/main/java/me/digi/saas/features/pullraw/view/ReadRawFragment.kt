package me.digi.saas.features.pullraw.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import me.digi.saas.R
import me.digi.saas.features.pullraw.viewmodel.ReadRawViewModel
import me.digi.saas.utils.Resource
import me.digi.saas.utils.snackBar
import me.digi.sdk.entities.DMEFileListItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ReadRawFragment: Fragment(R.layout.fragment_pull_raw) {

    private val viewModel: ReadRawViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getData()

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collectLatest { result: Resource<List<DMEFileListItem>> ->
                when(result) {
                    is Resource.Idle -> { /** Do nothing */ }
                    is Resource.Loading -> Timber.d("Loading")
                    is Resource.Success -> {
                        val data = result.data
                        Timber.d("Data: $data")
                    }
                    is Resource.Failure -> {
                        Timber.e(result.message ?: "Unknown error occurred")
                        snackBar(result.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }
}