package me.digi.ongoingpostbox.features.create.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_create_postbox.*
import kotlinx.coroutines.flow.collect
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.features.create.viewmodel.CreatePostboxViewModel
import me.digi.ongoingpostbox.features.upload.view.UploadContentFragment
import me.digi.ongoingpostbox.utils.Resource
import me.digi.ongoingpostbox.utils.replaceFragment
import me.digi.ongoingpostbox.utils.snackBarLong
import me.digi.sdk.entities.response.AuthorizationResponse
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class CreatePostboxFragment : Fragment(R.layout.fragment_create_postbox), View.OnClickListener {

    private val viewModel: CreatePostboxViewModel by viewModel()

    companion object {
        fun newInstance(): CreatePostboxFragment = CreatePostboxFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleClickListeners()
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.authState.collect { resource: Resource<AuthorizationResponse> ->
                when (resource) {
                    is Resource.Idle -> {
                        /**
                         * Do nothing
                         */
                    }
                    is Resource.Loading -> {
                        crePosProgressbar?.isVisible = true
                        btnCreatePostbox?.isEnabled = false
                        snackBarLong(getString(R.string.label_postbox_creation_started))
                    }
                    is Resource.Success -> {
                        crePosProgressbar?.isVisible = false
                        btnCreatePostbox?.isEnabled = true

                        val data = resource.data as AuthorizationResponse
                        Timber.d("Data: $data")

                        snackBarLong(getString(R.string.label_postbox_created))

                        UploadContentFragment
                            .newInstance()
                            .replaceFragment(requireActivity().supportFragmentManager)
                    }
                    is Resource.Failure -> {
                        crePosProgressbar?.isVisible = false
                        btnCreatePostbox?.isEnabled = true

                        snackBarLong(resource.message ?: getString(R.string.label_unknown_error))
                    }
                }
            }
        }
    }

    private fun handleClickListeners() {
        btnCreatePostbox?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCreatePostbox -> viewModel.createPostbox(requireActivity())
        }
    }
}