package me.digi.testimplementation

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import me.digi.sdk.DMEPullClient
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.interapp.DMEAppCommunicator
import kotlinx.android.synthetic.main.activity_main.*
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.DMEPushConfiguration
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    lateinit var pushClient: DMEPushClient
    lateinit var client: DMEPullClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        doCA()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    fun doPB() {
        val pk = DMECryptoUtilities(applicationContext).privateKeyHexFrom(
            applicationContext.getString(R.string.digime_p12_filename),
            applicationContext.getString(R.string.digime_p12_password)
        )
        val cfg = DMEPushConfiguration(
            applicationContext.getString(R.string.digime_application_id),
            applicationContext.getString(R.string.digime_contract_id)
        )
//        cfg.baseUrl = "https://api.test06.devdigi.me/"
        pushClient = DMEPushClient(applicationContext, cfg)

        launchBtn.setOnClickListener {
            pushClient.createPostbox(this) { dmePostbox, error ->
                if (dmePostbox != null) {
                    Log.i("DME", "Postbox Created: $dmePostbox")
                }
                else {
                    Log.i("DME", "Postbox Create Error: $error")
                }
            }
        }
    }

    fun doCA() {
        val pk = DMECryptoUtilities(applicationContext).privateKeyHexFrom(
            applicationContext.getString(R.string.digime_p12_filename),
            applicationContext.getString(R.string.digime_p12_password)
        )
        val cfg = DMEPullConfiguration(
            applicationContext.getString(R.string.digime_application_id),
            applicationContext.getString(R.string.digime_contract_id),
            pk
        )
        cfg.baseUrl = "https://api.integration.devdigi.me/"
        client = DMEPullClient(applicationContext, cfg)

        launchBtn.setOnClickListener {
            client.authorize(this) { session, error ->
                session?.let {
                    client.getSessionData({ file, error ->
                        Log.i("SDK File Received:", file.toString())
                    }) { error ->
                        Log.i("SDK File Complete", "")
                    }
                }
            }
        }
    }

}
