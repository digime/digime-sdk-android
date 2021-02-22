package me.digi.ongoingpostbox.features.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_intro.*
import me.digi.ongoingpostbox.MainActivity
import me.digi.ongoingpostbox.R

class IntroFragment: Fragment(R.layout.fragment_intro), View.OnClickListener {

    companion object {
        fun newInstance() = IntroFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        this.connectToDigimeBtn?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.connectToDigimeBtn -> (requireActivity() as MainActivity).proceedToSendDataFragment()
        }
    }
}