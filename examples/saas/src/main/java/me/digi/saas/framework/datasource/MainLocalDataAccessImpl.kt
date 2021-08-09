package me.digi.saas.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.entities.ContractHandler
import me.digi.saas.framework.utils.AppConst.CACHED_APP_ID
import me.digi.saas.framework.utils.AppConst.CACHED_BASE_URL
import me.digi.saas.framework.utils.AppConst.CACHED_CREDENTIAL_KEY
import me.digi.saas.framework.utils.AppConst.CACHED_POSTBOX_KEY
import me.digi.saas.framework.utils.AppConst.CACHED_PUSH_CONTRACT
import me.digi.saas.framework.utils.AppConst.CACHED_READ_CONTRACT
import me.digi.saas.framework.utils.AppConst.CACHED_READ_RAW_CONTRACT
import me.digi.saas.framework.utils.AppConst.CACHED_SESSION_KEY
import me.digi.saas.framework.utils.AppConst.CONTRACT_PREFS_KEY
import me.digi.saas.framework.utils.AppConst.SHAREDPREFS_KEY
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizeResponse

class MainLocalDataAccessImpl(private val context: Context) : MainLocalDataAccess {

    override fun getCachedCredential(): CredentialsPayload? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, CredentialsPayload::class.java)
            }
        }

    override fun getCachedPostbox(): OngoingPostboxData? =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_POSTBOX_KEY, null)?.let {
                Gson().fromJson(it, OngoingPostboxData::class.java)
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
                        val postbox = OngoingPostboxData().copy(
                            postboxId = credential.postboxId,
                            publicKey = credential.publicKey
                        )
                        val encodedPostbox = Gson().toJson(postbox)
                        putString(CACHED_POSTBOX_KEY, encodedPostbox)

                        val accessToken =
                            CredentialsPayload().copy(accessToken = AccessToken(value = credential.accessToken!!))
                        val encodedAccessToken = Gson().toJson(accessToken)
                        putString(CACHED_CREDENTIAL_KEY, encodedAccessToken)

                        apply()
                    }
                }
            }
        }

    override fun getCachedBaseUrl(): String? =
        context.getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_BASE_URL, null)?.let {
                Gson().fromJson(it, String::class.java)
            }
        }

    override fun getCachedAppId(): String? =
        context.getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_APP_ID, null)?.let {
                Gson().fromJson(it, String::class.java)
            }
        }

    override fun getCachedReadContract(): ContractHandler? =
        context.getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_READ_CONTRACT, null)?.let {
                Gson().fromJson(it, ContractHandler::class.java)
            }
        }

    override fun getCachedPushContract(): ContractHandler? =
        context.getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_PUSH_CONTRACT, null)?.let {
                Gson().fromJson(it, ContractHandler::class.java)
            }
        }

    override fun getCachedReadRawContract(): ContractHandler? =
        context.getSharedPreferences(CONTRACT_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_READ_RAW_CONTRACT, null)?.let {
                Gson().fromJson(it, ContractHandler::class.java)
            }
        }
}