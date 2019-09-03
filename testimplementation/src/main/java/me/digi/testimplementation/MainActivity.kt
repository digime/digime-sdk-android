package me.digi.testimplementation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import me.digi.sdk.DMEPullClient
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.entities.DMEPullClientConfiguration
import me.digi.sdk.interapp.DMEAppCommunicator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var client: DMEPullClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pk = DMECryptoUtilities(applicationContext).privateKeyHexFrom(
            applicationContext.getString(R.string.digime_p12_filename),
            applicationContext.getString(R.string.digime_p12_password)
        )
        val cfg = DMEPullClientConfiguration(
            applicationContext.getString(R.string.digime_application_id),
            applicationContext.getString(R.string.digime_contract_id),
            pk
        )
        client = DMEPullClient(applicationContext, cfg)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        launchBtn.setOnClickListener {
            client.authorize(this) { session, error ->
                session?.let {
//                    client.getSessionAccounts { accounts, error ->
//                        Log.i("DME", accounts?.toString())
//                        Log.i("DME", error?.message)
//                    }
                    client.getFileList { fileIds, error ->
                        Log.i("DME", fileIds?.toString())
                        Log.i("DME", error?.message)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }
}
