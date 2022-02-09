package saas.test.app.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import saas.test.app.data.localaccess.MainLocalDataAccess
import saas.test.app.entities.ContractHandler
import saas.test.app.framework.utils.AppConst.CACHED_APP_ID
import saas.test.app.framework.utils.AppConst.CACHED_BASE_URL
import saas.test.app.framework.utils.AppConst.CACHED_CREDENTIAL_KEY
import saas.test.app.framework.utils.AppConst.CACHED_POSTBOX_DATA
import saas.test.app.framework.utils.AppConst.CACHED_PUSH_CONTRACT
import saas.test.app.framework.utils.AppConst.CACHED_READ_CONTRACT
import saas.test.app.framework.utils.AppConst.CACHED_READ_RAW_CONTRACT
import saas.test.app.framework.utils.AppConst.CACHED_SESSION_DATA
import saas.test.app.framework.utils.AppConst.CONTRACT_PREFS_KEY
import saas.test.app.framework.utils.AppConst.SHARED_PREFS_KEY
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.ConsentAuthResponse

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
                    context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).edit()
                        .run {

                            /**
                             * Save session key
                             */
                            val encodedLocalSession = Gson().toJson(response.session)
                            putString(CACHED_SESSION_DATA, encodedLocalSession)

                            /**
                             * Save postbox data
                             */
                            if (response.authResponse?.postboxId != null && response.authResponse?.publicKey != null) {
                                val encodedLocalPostbox = Gson().toJson(response.authResponse)
                                putString(CACHED_POSTBOX_DATA, encodedLocalPostbox)
                            }

                            /**
                             * Save credentials data
                             */
                            val encodedCredentials = Gson().toJson(response.credentials)
                            putString(CACHED_CREDENTIAL_KEY, encodedCredentials)

                            apply()
                        }
                }
            }
        }

    override fun getCachedPostbox(): ConsentAuthResponse? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_POSTBOX_DATA, null)?.let {
                Gson().fromJson(it, ConsentAuthResponse::class.java)
            }
        }

    override fun getCachedSession(): Session? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_SESSION_DATA, null)?.let {
                Gson().fromJson(it, Session::class.java)
            }
        }

    override fun getCachedCredential(): CredentialsPayload? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, CredentialsPayload::class.java)
            }
        }
}