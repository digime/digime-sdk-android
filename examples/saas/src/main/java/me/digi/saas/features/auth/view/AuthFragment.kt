package me.digi.saas.features.auth.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import me.digi.saas.R
import me.digi.saas.features.auth.viewmodel.AuthViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class AuthFragment : Fragment(R.layout.fragment_auth), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Remove
        viewModel.authorize(requireActivity())

        setupClickListeners()
    }

    private fun setupClickListeners() {

    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.authenticate -> viewModel.authorize(requireActivity())
        }
    }
}