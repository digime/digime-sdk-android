package me.digi.examples.barebonesapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.digi.examples.barebonesapp.consentaccess.ConsentAccessActivity
import me.digi.examples.barebonesapp.postbox.PostboxActivity
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenPostboxEx.setOnClickListener {
            val intent = Intent(this, PostboxActivity::class.java)
            startActivity(intent)
        }
        btnOpenCAEx.setOnClickListener {
            val intent = Intent(this, ConsentAccessActivity::class.java)
            startActivity(intent)
        }
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
}
