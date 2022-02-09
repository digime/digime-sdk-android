package saas.test.app.features.details.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import saas.test.app.R
import saas.test.app.databinding.FragmentDetailsBinding
import saas.test.app.features.details.viewmodel.DetailsViewModel
import saas.test.app.utils.Resource
import saas.test.app.utils.snackBar
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileItemBytes
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val binding: FragmentDetailsBinding by viewBinding()
    private val viewModel: DetailsViewModel by viewModel()
    private var fileName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fileName = arguments?.getString("fileName", null)

        fileName?.let(viewModel::getFileByName)

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.state.collect { resource: Resource<FileItemBytes> ->
                when (resource) {
                    is Resource.Idle -> {
                        /**
                         * Do nothing
                         */
                    }
                    is Resource.Loading -> binding.pbDetails.isVisible = true
                    is Resource.Success -> {
                        binding.pbDetails.isVisible = false
                        binding.jsonLayoutDetails.bindJson(String(resource.data?.fileContent!!))
                    }
                    is Resource.Failure -> {
                        binding.pbDetails.isVisible = false
                        Timber.e(resource.message ?: "Unknown error occurred")
                        snackBar(resource.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }
}