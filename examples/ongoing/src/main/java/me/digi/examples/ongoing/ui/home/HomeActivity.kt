package me.digi.examples.ongoing.ui.home

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import me.digi.examples.ongoing.base.BaseActivity
import me.digi.examples.ongoing.service.DigiMeService
import me.digi.examples.ongoing.ui.home.fragments.ConnectToDigimeFragment
import me.digi.examples.ongoing.ui.home.fragments.ResultsFragment
import me.digi.ongoing.R
import me.digi.sdk.interapp.AppCommunicator
import kotlin.system.exitProcess

class HomeActivity : BaseActivity(R.layout.activity_home) {

    private val digiMeService: DigiMeService by lazy { DigiMeService(applicationContext as Application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (digiMeService.getCachedCredential()?.let {
            // Proceed straight to results screen, this isn't the user's first rodeo.
            ResultsFragment(digiMeService)
        } ?: run {
            // Show connect to digime screen first.
            ConnectToDigimeFragment()
        })
        .also { setFragment(R.id.homeRoot, it) }
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
        AppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    fun proceedToResultsFragment() {
        setFragment(R.id.homeRoot, ResultsFragment(digiMeService))
    }

}