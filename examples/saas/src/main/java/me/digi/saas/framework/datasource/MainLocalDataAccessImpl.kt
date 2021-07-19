package me.digi.saas.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.framework.utils.AppConst.CACHED_CREDENTIAL_KEY
import me.digi.saas.framework.utils.AppConst.CACHED_POSTBOX_KEY
import me.digi.saas.framework.utils.AppConst.CACHED_SESSION_KEY
import me.digi.saas.framework.utils.AppConst.SHAREDPREFS_KEY
import me.digi.sdk.entities.*

class MainLocalDataAccessImpl(private val context: Context): MainLocalDataAccess {

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

    override fun getCachedSession(): Session? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_SESSION_KEY, null)?.let {
                Gson().fromJson(it, Session::class.java)
            }
        }

    override fun cacheAuthSessionCredentials(): SingleTransformer<AuthorizeResponse?, AuthorizeResponse?> =
        SingleTransformer {
            it.map { credential ->
                credential?.apply {
                    context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).edit().run {
                        val postbox = DMEOngoingPostboxData().copy(postboxId = credential.postboxId, publicKey = credential.publicKey)
                        val encodedPostbox = Gson().toJson(postbox)
                        putString(CACHED_POSTBOX_KEY, encodedPostbox)

                        val accessToken = DMETokenExchange().copy(accessToken = AccessToken(value = credential.accessToken!!))
                        val encodedAccessToken = Gson().toJson(accessToken)
                        putString(CACHED_CREDENTIAL_KEY, encodedAccessToken)

                        apply()
                    }
                }
            }
        }
}