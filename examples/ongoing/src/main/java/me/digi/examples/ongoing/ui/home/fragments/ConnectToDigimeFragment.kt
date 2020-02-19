package me.digi.examples.ongoing.ui.home.fragments

import android.app.Activity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_connect_to_digime.*
import me.digi.examples.ongoing.base.BaseFragment
import me.digi.examples.ongoing.service.DigiMeService
import me.digi.ongoing.R


class ConnectToDigimeFragment : BaseFragment(R.layout.fragment_connect_to_digime) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonPlugMeIn.setOnClickListener { openDigime() }
    }

    private fun openDigime() {
        val manager = fragmentManager
        val transaction = manager?.beginTransaction()
        transaction?.replace(R.id.homeRoot, LoadDigimeDataFragment())
        transaction?.commit()
    }

}