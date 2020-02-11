package me.digi.examples.ongoing.ui.home

import android.os.Bundle
import me.digi.examples.ongoing.base.BaseActivity
import me.digi.examples.ongoing.ui.home.fragments.ConnectToDigimeFragment
import me.digi.ongoing.R
import me.digi.sdk.DMEPullClient

class HomeActivity : BaseActivity(R.layout.activity_home) {

    lateinit var client : DMEPullClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragment(R.id.homeRoot, ConnectToDigimeFragment())
    }

}