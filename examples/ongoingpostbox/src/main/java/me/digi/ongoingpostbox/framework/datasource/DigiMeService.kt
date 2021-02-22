package me.digi.ongoingpostbox.framework.datasource

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.framework.utils.authorizeOngoingPostbox
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPushConfiguration
import me.digi.sdk.utilities.crypto.DMECryptoUtilities

class DigiMeService(private val context: Application) {

    companion object {
        private const val SHAREDPREFS_KEY = "DigiMeXShareableSharedPreferences"
        private const val CACHED_CREDENTIAL_KEY = "CachedCredential"
    }

    private val client: DMEPushClient by lazy {

        val privateKey = DMECryptoUtilities(context).privateKeyHexFrom(
            context.getString(R.string.digime_p12_filename),
            context.getString(R.string.digime_p12_password)
        )

        val configuration = DMEPushConfiguration(
            context.getString(R.string.digime_application_id),
            context.getString(R.string.digime_contract_id),
            privateKey
        )

        DMEPushClient(context, configuration)
    }

    fun obtainAccessRights(activity: Activity): Completable =
        client.authorizeOngoingPostbox(activity, getCachedCredential())
            .map { it.second }
            .compose(cacheCredentials())
            .flatMapCompletable { Completable.complete() }

    fun getCachedCredential(): DMEOAuthToken? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, DMEOAuthToken::class.java)
            }
        }

    private fun cacheCredentials(): SingleTransformer<DMEOAuthToken, DMEOAuthToken> =
        SingleTransformer {
            it.map { credential ->
                credential?.apply {
                    context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).edit().run {
                        val encodedCredential = Gson().toJson(credential)
                        putString(CACHED_CREDENTIAL_KEY, encodedCredential)
                        apply()
                    }
                }
            }
        }
}