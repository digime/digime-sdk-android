package me.digi.ongoingpostbox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.features.base.BaseActivity
import me.digi.ongoingpostbox.features.intro.IntroFragment
import me.digi.ongoingpostbox.features.send.SendDataFragment
import me.digi.sdk.interapp.DMEAppCommunicator
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    private val localAccess: MainLocalDataAccess by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Digimesdkandroid)
        setContentView(R.layout.activity_main)

        (localAccess.getCachedCredential()?.let {
            // Proceed straight to results screen, this isn't the user's first rodeo.
            SendDataFragment.newInstance()
        } ?: run {
            // Show connect to digime screen first.
            IntroFragment()
        })
            .also { setFragment(R.id.homeRoot, it) }
    }

    override fun onResume() {
        super.onResume()

        // Check that an Application ID has been configured.
        if (getString(R.string.digime_application_id).isEmpty()) {

            val msg = AlertDialog.Builder(this)
            msg.setTitle("Missing Application ID")
            msg.setMessage(
                """
                You must provide an application ID in strings.xml.
                Please follow the instructions in the README to obtain yours.
                
                The application will now exit.
                """.trimIndent()
            )
            msg.setNeutralButton("Okay") { _, _ -> exitProcess(1) }
            msg.create().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    fun proceedToSendDataFragment() {
        setFragment(R.id.homeRoot, SendDataFragment.newInstance())
    }
}