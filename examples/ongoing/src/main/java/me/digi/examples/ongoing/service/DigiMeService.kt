package me.digi.examples.ongoing.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.digi.examples.ongoing.model.Song
import me.digi.ongoing.R
import me.digi.sdk.DMEPullClient
import me.digi.sdk.callbacks.DMEOngoingAuthorizationCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.utilities.crypto.DMECryptoUtilities

object DigiMeService {

    lateinit var client : DMEPullClient
    private val songs = listOf<Song>().toMutableList()

    fun configureSdk(context: Context) {
        val privateKey = DMECryptoUtilities(context).privateKeyHexFrom( context.getString(R.string.digime_p12_filename), context.getString(R.string.digime_p12_password) )
        val configuration = DMEPullConfiguration( context.getString(R.string.digime_application_id), context.getString(R.string.digime_contract_id), privateKey )
        client = DMEPullClient(context, configuration)
    }

    fun requestConsent(activity: Activity, completion: DMEOngoingAuthorizationCompletion) {
        val objects = listOf(DMEServiceObjectType(406))
        val services = listOf(DMEServiceType(19, objects))
        val groups = listOf(DMEServiceGroup(5, services))
        val scope = DMEScope().apply { timeRanges = listOf(DMETimeRange(to = null, from = null, last = "1d", type = null)) }
        scope.serviceGroups = groups

        val credStore = activity.getSharedPreferences("Default", Context.MODE_PRIVATE)
        val creds = credStore.getString("Token", null)?.let {
            Gson().fromJson(it, DMEOAuthToken::class.java)
        }

        client.authorizeOngoingAccess(activity, scope, creds) { session, credentials, error ->
            if (session != null && credentials != null) {
                val credentialsJson = Gson().toJson(credentials)
                credStore.edit().putString("Token", credentialsJson).apply()
            }
            else { Log.e("SDK Ongoing Access", error.toString()) }
            completion(session, credentials, error)
        }
    }

    fun getData(completion: (List<Song>) -> Unit) {
        client.getSessionData({file, error ->
            if (file != null) {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val songs = gson.fromJson<List<Song>>(String(file.fileContent), object : TypeToken<List<Song>>(){}.type)
                this.songs += songs
            }
            else { Log.e("SDK Error", error.toString()) }
        }) { fileList, error ->
            if (fileList != null) {
                completion(this.songs)
                Log.i("File List", fileList.toString())
            }
            else { Log.e("SDK Error", error.toString()) }
        }
    }

}