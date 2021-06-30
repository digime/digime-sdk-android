package me.digi.ongoingpostbox.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.framework.utils.AppConst.CACHED_CREDENTIAL_KEY
import me.digi.ongoingpostbox.framework.utils.AppConst.CACHED_POSTBOX_KEY
import me.digi.ongoingpostbox.framework.utils.AppConst.CACHED_SESSION_KEY
import me.digi.ongoingpostbox.framework.utils.AppConst.SHAREDPREFS_KEY
import me.digi.sdk.entities.DMEOngoingPostboxData
import me.digi.sdk.entities.DMESaasOngoingPostbox
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.Session

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

    override fun getCachedCredential(): DMETokenExchange? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, DMETokenExchange::class.java)
            }
        }

    override fun getCachedPostbox(): DMEOngoingPostboxData? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_POSTBOX_KEY, null)?.let {
                Gson().fromJson(it, DMEOngoingPostboxData::class.java)
            }
        }

    override fun getCachesSession(): Session? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_SESSION_KEY, null)?.let {
                Gson().fromJson(it, Session::class.java)
            }
        }

    override fun cacheCredentials(): SingleTransformer<DMESaasOngoingPostbox?, DMESaasOngoingPostbox?> =
        SingleTransformer {
            it.map { credential ->
                credential?.apply {
                    context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).edit().run {
                        val encodedPostbox = Gson().toJson(credential.postboxData)
                        val encodedCredential = Gson().toJson(credential.authToken)
                        val encodedSession = Gson().toJson(credential.session)
                        putString(CACHED_CREDENTIAL_KEY, encodedCredential)
                        putString(CACHED_POSTBOX_KEY, encodedPostbox)
                        putString(CACHED_SESSION_KEY, encodedSession)
                        apply()
                    }
                }
            }
        }
}