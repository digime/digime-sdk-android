package me.digi.examples.ongoing.service

import android.app.Activity
import android.content.Context
import android.util.Log
import me.digi.examples.ongoing.model.Song
import me.digi.ongoing.R
import me.digi.sdk.DMEPullClient
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.utilities.crypto.DMECryptoUtilities

object DigiMeService {

    lateinit var client : DMEPullClient
    val songs = listOf<Song>().toMutableList()

    fun configureSdk(context: Context) {
        val privateKey = DMECryptoUtilities(context).privateKeyHexFrom(
            context.getString(R.string.digime_p12_filename),
            context.getString(R.string.digime_p12_password)
        )

        val configuration = DMEPullConfiguration(
            context.getString(R.string.digime_application_id),
            context.getString(R.string.digime_contract_id),
            privateKey
        )

        client = DMEPullClient(context, configuration)
    }

    fun requestConsent(activity: Activity, completion: DMEAuthorizationCompletion) {
        val objects = listOf(DMEServiceObjectType(406))
        val services = listOf(DMEServiceType(19, objects))
        val groups = listOf(DMEServiceGroup(5, services))
        val scope = DMEScope()
        scope.serviceGroups = groups
        client.authorize(activity, scope, completion)
    }

    fun getData(completion: (List<Song>) -> Unit) {
        client.getSessionData({file, error ->
            if (file == null) {}
        }) { fileList, error ->
            completion(this.songs)
            Log.e("SDK Error", error.toString())
        }
    }

}