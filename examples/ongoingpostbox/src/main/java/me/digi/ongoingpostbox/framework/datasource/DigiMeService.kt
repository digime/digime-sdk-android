package me.digi.ongoingpostbox.framework.datasource

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.framework.utils.authorizeOngoingPostbox
import me.digi.ongoingpostbox.framework.utils.pushData
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.entities.DMEPushConfiguration
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import timber.log.Timber

class DigiMeService(private val context: Application) {

    companion object {
        private const val SHAREDPREFS_KEY = "DigiMeXShareableSharedPreferences"
        private const val CACHED_CREDENTIAL_KEY = "CachedCredential"
        private const val CACHED_POSTBOX_KEY = "CachedPostbox"
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

    fun obtainAccessRights(activity: Activity): Single<Pair<DMEPostbox?, DMEOAuthToken?>> =
        client.authorizeOngoingPostbox(activity, getCachedPostbox(), getCachedCredential())
            .map {
                Timber.d("AccessRights: $it")
                it
            }
            .compose(cacheCredentials())
            .map { it }

    fun getCachedCredential(): DMEOAuthToken? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, DMEOAuthToken::class.java)
            }
        }

    fun getCachedPostbox(): DMEPostbox? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_POSTBOX_KEY, null)?.let {
                Gson().fromJson(it, DMEPostbox::class.java)
            }
        }

    private fun cacheCredentials(): SingleTransformer<Pair<DMEPostbox?, DMEOAuthToken?>, Pair<DMEPostbox?, DMEOAuthToken?>> =
        SingleTransformer {
            it.map { credential ->
                credential?.apply {
                    context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).edit().run {
                        val encodedPostbox = Gson().toJson(credential.first)
                        val encodedCredential = Gson().toJson(credential.second)
                        putString(CACHED_CREDENTIAL_KEY, encodedCredential)
                        putString(CACHED_POSTBOX_KEY, encodedPostbox)
                        apply()
                    }
                }
            }
        }

    fun pushDataToOngoingPostbox(first: DMEPushPayload? = null, second: DMEOAuthToken? = null) {
        client.pushData(first, second).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onSuccess = {
                    Timber.d("Data pushed!")
                },
                onError = {
                    Timber.e("Error occurred: ${it.localizedMessage ?: "Unknown"}")
                }
            )
    }
}