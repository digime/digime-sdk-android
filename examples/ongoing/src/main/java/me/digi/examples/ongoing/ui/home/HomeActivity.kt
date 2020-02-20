package me.digi.examples.ongoing.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import me.digi.examples.ongoing.base.BaseActivity
import me.digi.examples.ongoing.ui.home.fragments.ConnectToDigimeFragment
import me.digi.examples.ongoing.ui.home.fragments.LoadDigimeDataFragment
import me.digi.ongoing.R
import me.digi.sdk.interapp.DMEAppCommunicator
import kotlin.system.exitProcess

class HomeActivity : BaseActivity(R.layout.activity_home) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("Default", Context.MODE_PRIVATE)
        val isRestoringAccess = prefs.getString("Token", null) != null
        setFragment(R.id.homeRoot, if (isRestoringAccess) LoadDigimeDataFragment() else ConnectToDigimeFragment())
    }

    override fun onResume() {
        super.onResume()

        // Check that an Application ID has been configured.
        if (getString(R.string.digime_application_id).isEmpty()) {

            val msg = AlertDialog.Builder(this)
            msg.setTitle("Missing Application ID")
            msg.setMessage("""
                You must provide an application ID in strings.xml.
                Please follow the instructions in the README to obtain yours.
                
                The application will now exit.
                """.trimIndent())
            msg.setNeutralButton("Okay") { _, _ ->
                exitProcess(1)
            }
            msg.create().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

}