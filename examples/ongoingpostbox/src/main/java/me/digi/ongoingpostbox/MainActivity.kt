package me.digi.ongoingpostbox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.features.create.view.CreatePostboxFragment
import me.digi.ongoingpostbox.features.upload.view.UploadContentFragment
import me.digi.ongoingpostbox.utils.replaceFragment
import me.digi.sdk.interapp.AppCommunicator
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val localAccess: MainLocalDataAccess by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Digimesdkandroid)
        setContentView(R.layout.activity_main)

        // User has all needed information to push their data
        if (localAccess.getCachedCredential() != null && localAccess.getCachedPostbox() != null && localAccess.getCachedSession() != null)
            UploadContentFragment.newInstance().replaceFragment(supportFragmentManager)
        else CreatePostboxFragment.newInstance().replaceFragment(supportFragmentManager)
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
        AppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }
}