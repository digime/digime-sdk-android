package me.digi.testimplementation

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import me.digi.sdk.DMEPullClient
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.entities.DMEPullClientConfiguration

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

        client.authorize { session, error ->
            Log.i("DME", session.toString())
            Log.i("DME", error.toString())
        }

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
    }
}
