package me.digi.examples.barebonesapp.postbox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.postbox_activity_layout.*
import me.digi.examples.barebonesapp.R
import me.digi.examples.barebonesapp.util.ConsentAccessInProgress
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEMimeType
import me.digi.sdk.entities.DMEPushConfiguration
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.interapp.DMEAppCommunicator
import java.io.IOException

class PostboxActivity : AppCompatActivity() {
    private lateinit var client: DMEPushClient
    private lateinit var cfg: DMEPushConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.postbox_activity_layout)

        cfg = DMEPushConfiguration(
            applicationContext.getString(R.string.digime_application_id),
            applicationContext.getString(R.string.digime_postbox_contract_id)
        )

        client = DMEPushClient(applicationContext, cfg)

        item_postbox_button.setOnClickListener {
            if(DMEAppCommunicator.getSharedInstance().canOpenDMEApp())
                createPostbox()
            else
                Toast.makeText(this, "Please install digi.me in order to continue", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPostbox() {
        displaySendingData()

        client.createPostbox(this) { dmePostbox, error ->
            if (dmePostbox != null) {

                val fileContent = getFileContent("file.png")
                val metadata = getFileContent("metadatapng.json")

                client.pushDataToPostbox(
                    DMEPushPayload(
                        dmePostbox,
                        metadata,
                        fileContent,
                        DMEMimeType.IMAGE_PNG
                    )
                ) {
                    if (error == null) {
                        displayResults()
                    } else removeSending(error.message)
                }
            } else {
                Log.i("DME", "Postbox Create Error: $error")
                error?.message?.let { removeSending(it) }
            }
        }
    }

    private fun getFileContent(fileName: String): ByteArray {
        return try {
            val stream = assets.open(fileName)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            buffer
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ByteArray(2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    private fun displayResults() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PostboxFragment())
            .commit()
    }

    private fun removeSending(errorMessage: String) {
        supportFragmentManager.popBackStack()
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun displaySendingData() {
        val bundle = Bundle()
        bundle.putString("progressText", "Sending data")

        val sendingDataFragment = ConsentAccessInProgress()
        sendingDataFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .addToBackStack("in_progress")
            .replace(android.R.id.content, sendingDataFragment)
            .commit()
    }
}