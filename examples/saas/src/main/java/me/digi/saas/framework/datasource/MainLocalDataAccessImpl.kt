package me.digi.saas.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.entities.AuthData
import me.digi.saas.entities.ContractHandler
import me.digi.saas.framework.utils.AppConst.CACHED_APP_ID
import me.digi.saas.framework.utils.AppConst.CACHED_AUTH_DATA
import me.digi.saas.framework.utils.AppConst.CACHED_BASE_URL
import me.digi.saas.framework.utils.AppConst.CACHED_CREDENTIAL_KEY
import me.digi.saas.framework.utils.AppConst.CACHED_PUSH_CONTRACT
import me.digi.saas.framework.utils.AppConst.CACHED_READ_CONTRACT
import me.digi.saas.framework.utils.AppConst.CACHED_READ_RAW_CONTRACT
import me.digi.saas.framework.utils.AppConst.CONTRACT_PREFS_KEY
import me.digi.saas.framework.utils.AppConst.SHARED_PREFS_KEY
import me.digi.sdk.entities.payload.AccessToken
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.RefreshToken
import me.digi.sdk.entities.response.AuthorizationResponse

class MainLocalDataAccessImpl(private val context: Context) : MainLocalDataAccess {

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

    override fun cacheAuthorizationData(): SingleTransformer<in AuthorizationResponse, out AuthorizationResponse> =
        SingleTransformer<AuthorizationResponse, AuthorizationResponse> {
            it.map { response ->
                response?.apply {
                    context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).edit().run {

                        val data = AuthData().copy(
                            sessionKey = response.sessionKey,
                            postboxId = response.postboxData?.postboxId,
                            publicKey = response.postboxData?.publicKey
                        )
                        val encodedData = Gson().toJson(data)
                        putString(CACHED_AUTH_DATA, encodedData)

                        val accessToken = CredentialsPayload().copy(
                            accessToken = AccessToken(value = response.credentials?.accessToken),
                            refreshToken = RefreshToken(value = response.credentials?.refreshToken)
                        )
                        val encodedAccessToken = Gson().toJson(accessToken)
                        putString(CACHED_CREDENTIAL_KEY, encodedAccessToken)

                        apply()
                    }
                }
            }
        }

    override fun getCachedAuthData(): AuthData? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_AUTH_DATA, null)?.let {
                Gson().fromJson(it, AuthData::class.java)
            }
        }

    override fun getCachedCredential(): CredentialsPayload? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, CredentialsPayload::class.java)
            }
        }
}