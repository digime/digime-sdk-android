package me.digi.examples.barebonesapp.consentaccess

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.consent_access_activity_layout.*
import me.digi.barebonesapp.consentaccess.ConsentAccessFragment
import me.digi.barebonesapp.util.ConsentAccesInProgress
import me.digi.examples.barebonesapp.R
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.crypto.DMECryptoUtilities

class ConsentAccessActivity : AppCompatActivity() {
    private lateinit var client: DMEPullClient
    private lateinit var pk: String
    private lateinit var cfg: DMEPullConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.consent_access_activity_layout)

        pk = DMECryptoUtilities(applicationContext).privateKeyHexFrom(
                applicationContext.getString(R.string.digime_p12_filename),
                applicationContext.getString(R.string.digime_p12_password)
        )

        cfg = DMEPullConfiguration(
                applicationContext.getString(R.string.digime_application_id),
                applicationContext.getString(R.string.digime_contract_id),
                pk
        )

        cfg.baseUrl = "https://api.digi.me/"

        item_ca_button_share_digime.setOnClickListener {
            displayReceiving()
            shareViaDigiMe()
        }

        item_ca_button_share_guest.setOnClickListener {
            displayReceiving()
            shareAsGuest()
        }

    }



    private fun shareViaDigiMe() {
        var receivedFiles = 0

        client = DMEPullClient(applicationContext, cfg)

        client.authorize(this) { session, error ->
            session?.let {
                client.getSessionData({ file, _ ->
                    receivedFiles++
                    Log.d("File received ", file.toString())
                })
                {
                    if(receivedFiles > 0)
                        removeReceiving("")
                }
            }
            error?.message?.let { it -> removeReceiving(it) }
        }
    }


    private fun shareAsGuest() {
        var receivedFiles = 0

        client = DMEPullClient(applicationContext, cfg)
        cfg.guestEnabled = true

        client.authorize(this) { session, error ->
            session?.let {
                client.getSessionData({ file, _ ->
                    receivedFiles++
                    Log.d("File received ", file.toString())
                })
                {
                    if(receivedFiles > 0)
                        removeReceiving("")
                }
            }
            error?.message?.let { it -> removeReceiving(it) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    private fun displayReceiving() {
        val bundle = Bundle()
        bundle.putString("progressText", "Receiving data")

        val sendingDataFragment = ConsentAccesInProgress()
        sendingDataFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
                .addToBackStack("in_progress")
                .replace(android.R.id.content, sendingDataFragment)
                .commit()
    }

    private fun displayResult() {
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, ConsentAccessFragment())
                .addToBackStack("shady_car_insurance_fragment")
                .commit()
    }

    private fun removeReceiving(errorMessage: String) {
        supportFragmentManager.popBackStack()
        if (errorMessage.isNotEmpty())
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()

        displayResult()
    }
}