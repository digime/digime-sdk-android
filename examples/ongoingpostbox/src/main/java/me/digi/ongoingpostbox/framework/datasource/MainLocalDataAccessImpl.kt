package me.digi.ongoingpostbox.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox

/**
 * Idea behind local main data access is to isolate
 * local calls from remote ones.
 * In which case in the repository/ies we can call one
 * or the other, or combine them to get seamless data access flow
 *
 * In our case here, given we're working with small amount of data
 * we're using [SharedPreferences]
 */
class MainLocalDataAccessImpl(private val context: Context) : MainLocalDataAccess {

    companion object {
        private const val SHAREDPREFS_KEY = "DigiMeXShareableSharedPreferences"
        private const val CACHED_CREDENTIAL_KEY = "CachedCredential"
        private const val CACHED_POSTBOX_KEY = "CachedPostbox"
    }

    override fun getCachedCredential(): DMEOAuthToken? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, DMEOAuthToken::class.java)
            }
        }

    override fun getCachedPostbox(): DMEPostbox? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_POSTBOX_KEY, null)?.let {
                Gson().fromJson(it, DMEPostbox::class.java)
            }
        }

    override fun cacheCredentials(): SingleTransformer<Pair<DMEPostbox?, DMEOAuthToken?>, Pair<DMEPostbox?, DMEOAuthToken?>> =
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
}