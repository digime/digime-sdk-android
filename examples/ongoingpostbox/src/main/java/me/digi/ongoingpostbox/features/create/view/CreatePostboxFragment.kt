package me.digi.ongoingpostbox.features.create.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_create_postbox.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.domain.OngoingPostboxResponseBody
import me.digi.ongoingpostbox.features.create.viewmodel.CreatePostboxViewModel
import me.digi.ongoingpostbox.features.upload.view.UploadContentFragment
import me.digi.ongoingpostbox.utils.Resource
import me.digi.ongoingpostbox.utils.replaceFragment
import me.digi.ongoingpostbox.utils.snackBar
import org.koin.android.viewmodel.ext.android.viewModel

class CreatePostboxFragment : Fragment(R.layout.fragment_create_postbox), View.OnClickListener {

    private val viewModel: CreatePostboxViewModel by viewModel()

    private var firstExecution: Boolean = true

    companion object {
        fun newInstance() = CreatePostboxFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
        handleClickListeners()
    }

    private fun handleClickListeners() {
        btnCreatePostbox?.setOnClickListener(this)
    }

    private fun subscribeToObservers() {
        viewModel.createPostboxStatus.observe(
            viewLifecycleOwner,
            Observer { result: Resource<OngoingPostboxResponseBody> ->
                when (result) {
                    is Resource.Loading -> {
                        crePosProgressbar?.isVisible = true
                        btnCreatePostbox?.isEnabled = false
                    }
                    is Resource.Success -> {
                        crePosProgressbar?.isVisible = false
                        handlePostboxSuccess(result.data!!)
                    }
                    is Resource.Failure -> {
                        crePosProgressbar?.isVisible = false
                        btnCreatePostbox?.isEnabled = true
                        snackBar(result.message ?: "Unknown")
                    }
                }
            })
    }

    private fun handlePostboxSuccess(result: OngoingPostboxResponseBody) =
        UploadContentFragment
            .newInstance(result)
            .replaceFragment(requireActivity().supportFragmentManager)

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCreatePostbox -> {
                if (firstExecution) {
                    viewModel.createPostbox(requireActivity())
                    firstExecution = false
                }
            }
        }
    }
}