package me.digi.examples.testapp

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ca_flow.*
import me.digi.sdk.DMEError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.DMEScope
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.crypto.DMECryptoUtilities

class CaFlow : AppCompatActivity() {
    private lateinit var client: DMEPullClient
    private lateinit var pk: String
    private lateinit var cfg: DMEPullConfiguration
    private var test : DMEScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ca_flow)

        if(intent != null) {
            val gson = Gson()
            test = gson.fromJson<DMEScope>(intent.getStringExtra("DMEScope"), DMEScope::class.java)
        }

        pk = DMECryptoUtilities(applicationContext).privateKeyHexFrom(
            applicationContext.getString(R.string.digime_p12_filename)
        )

        cfg = DMEPullConfiguration(
            applicationContext.getString(R.string.digime_application_id),
            applicationContext.getString(R.string.digime_contract_id),
            applicationContext.getString(R.string.digime_p12_filename)
        )

        console_log.movementMethod = ScrollingMovementMethod()

        start_ca_flow.setOnClickListener {
            updateConsoleLog("Ca flow started...")
            shareViaDigiMe()
        }
    }

    fun updateConsoleLog(text: String) {
        console_log.append(text + "\n")
    }

    class DMEServiceGroup (
        val id: Int,
        val serviceTypes: List<DMEServiceType>
    )

    class DMEServiceType (
        val id: Int,
        val objectTypes: List<Int>
    )

    private fun shareViaDigiMe() {
//        cfg.baseUrl = "https://api.development.devdigi.me/"
        client = DMEPullClient(applicationContext, cfg)
        cfg.guestEnabled = true

        //        val serviceObjectTypes: List<DMEServiceObjectType> = mutableListOf()
//        val serviceObjectTypes: List<DMEServiceObjectType> = listOf(DMEServiceObjectType(1), DMEServiceObjectType(2), DMEServiceObjectType(7))
//        val serviceTypes: List<DMEServiceType> = listOf(DMEServiceType(4, serviceObjectTypes), DMEServiceType(1, serviceObjectTypes))
//        val serviceGroups: List<DMEServiceGroup> = listOf(DMEServiceGroup(2, serviceTypes))
//
//
//        val lalala = DMEScope()
//        lalala.serviceGroups = serviceGroups
//
//        val data = Gson().fromJson("[{\"id\":1,\"serviceTypes\":[{\"id\":1,\"objectTypes\":[1,2]}]}]", Array<DMEServiceGroup>::class.java)
//
//        val scope = DMEScope()
//        scope.timeRanges = listOf(DMETimeRange(null, null, null, "all"))

//        scope.serviceGroups = data.map { it ->
//            DMEServiceGroup(it.id, it.serviceTypes.map { service ->
//                DMEServiceType(service.id, service.objectTypes.map { objectType ->
//                    DMEServiceObjectType(objectType)
//                })
//            })
//        }

        client.authorize(this) { authSession, error ->

        }

//        client.authorize(this, test) { session, error: DMEError? ->
//            session?.let {
//                client.onboard(this, session) {
//                    client.getFileList{ fileList, error ->
//                        val aaa = 0
//                    }
//                }
//
////                updateConsoleLog("\nClient version: " + session.metadata["digiMeVersion"])
////                client.getSessionData({ file, error ->
////                    if (file != null) {
////                        updateConsoleLog(file.identifier + " success")
////                    } else if(error != null) {
////                        updateConsoleLog("Error downloading file")
////                    }
////                })
////                {_, error ->
////                    if (error == null) {
////                        updateConsoleLog("\nFinished getting files")
////                        updateConsoleLog("Getting accounts")
////                        getAccounts()
////                    }
////                    else{
////                        updateConsoleLog("\nFinished getting files with error: " + error.message)
////                        updateConsoleLog("Getting accounts")
////                        getAccounts()
////                    }
////                }
//            }
//            error?.message?.let { it -> updateConsoleLog("Error downloading file: " + error.message) }
//        }
    }

    private fun getAccounts() {
        client.getSessionAccounts { accounts, error ->
            if(accounts != null){
                updateConsoleLog("Downloaded num of accounts: " + accounts.size.toString())
                accounts.forEach {
                    updateConsoleLog("ACCOUNT: " + it.id)
                    updateConsoleLog("         " + it.name)
                    updateConsoleLog("         " + it.service.name)
                    updateConsoleLog("         " + it.service.logoUrl)
                }
            }
            else
            {
                updateConsoleLog("Failed to download account")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }
}