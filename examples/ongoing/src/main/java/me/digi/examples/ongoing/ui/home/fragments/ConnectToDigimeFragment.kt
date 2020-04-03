package me.digi.examples.ongoing.ui.home.fragments

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_connect_to_digime.*
import me.digi.examples.ongoing.base.BaseFragment
import me.digi.examples.ongoing.ui.home.HomeActivity
import me.digi.ongoing.R


class ConnectToDigimeFragment : BaseFragment(R.layout.fragment_connect_to_digime) {

    private val parent: HomeActivity by lazy { activity as HomeActivity }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonPlugMeIn.setOnClickListener { parent.proceedToResultsFragment() }
    }
}