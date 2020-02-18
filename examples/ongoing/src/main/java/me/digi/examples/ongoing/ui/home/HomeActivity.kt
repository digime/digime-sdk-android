package me.digi.examples.ongoing.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.digi.examples.ongoing.base.BaseActivity
import me.digi.examples.ongoing.ui.home.fragments.ConnectToDigimeFragment
import me.digi.examples.ongoing.ui.home.fragments.LoadDigimeDataFragment
import me.digi.ongoing.R
import me.digi.sdk.interapp.DMEAppCommunicator

class HomeActivity : BaseActivity(R.layout.activity_home) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("Default", Context.MODE_PRIVATE)
        val isRestoringAccess = prefs.getString("Token", null) != null
        setFragment(R.id.homeRoot, if (isRestoringAccess) LoadDigimeDataFragment() else ConnectToDigimeFragment())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

}