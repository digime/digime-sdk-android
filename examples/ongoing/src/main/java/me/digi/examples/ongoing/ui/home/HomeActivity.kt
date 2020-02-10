package me.digi.examples.ongoing.ui.home

import android.os.Bundle
import me.digi.examples.ongoing.base.BaseActivity
import me.digi.examples.ongoing.ui.home.fragments.ConnectToDigimeFragment
import me.digi.ongoing.R

class HomeActivity : BaseActivity(R.layout.activity_home) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragment(R.id.homeRoot, ConnectToDigimeFragment())
    }

}