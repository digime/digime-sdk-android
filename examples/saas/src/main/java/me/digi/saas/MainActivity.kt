package me.digi.saas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.digi.sdk.interapp.DMEAppCommunicator
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        // Check that an Application ID has been configured.
        if (getString(R.string.appId).isEmpty()) {

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
}