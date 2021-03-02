package me.digi.ongoingpostbox.features.create.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_create_postbox.*
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.features.upload.view.UploadContentFragment
import me.digi.ongoingpostbox.utils.replaceFragment

class CreatePostboxFragment : Fragment(R.layout.fragment_create_postbox), View.OnClickListener {

    companion object {
        fun newInstance() = CreatePostboxFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleClickListeners()
    }

    private fun handleClickListeners() {
        btnCreatePostbox?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCreatePostbox -> UploadContentFragment
                .newInstance()
                .replaceFragment(requireActivity().supportFragmentManager)
        }
    }
}