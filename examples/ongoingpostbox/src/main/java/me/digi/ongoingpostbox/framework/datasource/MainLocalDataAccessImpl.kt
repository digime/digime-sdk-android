package me.digi.ongoingpostbox.framework.datasource

import android.content.Context
import com.google.gson.Gson
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.domain.LocalSession
import me.digi.ongoingpostbox.framework.utils.AppConst.CACHED_CREDENTIAL_KEY
import me.digi.ongoingpostbox.framework.utils.AppConst.CACHED_POSTBOX_DATA
import me.digi.ongoingpostbox.framework.utils.AppConst.CACHED_SESSION_DATA
import me.digi.ongoingpostbox.framework.utils.AppConst.SHARED_PREFS_KEY
import me.digi.sdk.entities.OngoingPostboxData
import me.digi.sdk.entities.payload.AccessToken
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.RefreshToken
import me.digi.sdk.entities.response.AuthorizationResponse

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

    override fun cacheAuthorizationData(): SingleTransformer<in AuthorizationResponse, out AuthorizationResponse> =
        SingleTransformer<AuthorizationResponse, AuthorizationResponse> {
            it.map { response ->
                response?.apply {
                    context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).edit()
                        .run {

                            /**
                             * Save session key
                             */
                            val sessionData = LocalSession().copy(sessionKey = response.sessionKey)
                            val encodedLocalSession = Gson().toJson(sessionData)
                            putString(CACHED_SESSION_DATA, encodedLocalSession)

                            /**
                             * Save postbox data
                             */
                            val postboxData = OngoingPostboxData().copy(
                                postboxId = response.postboxData?.postboxId,
                                publicKey = response.postboxData?.publicKey
                            )
                            val encodedLocalPostbox = Gson().toJson(postboxData)
                            putString(CACHED_POSTBOX_DATA, encodedLocalPostbox)

                            /**
                             * Save credentials data
                             */
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

    override fun getCachedSession(): LocalSession? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_SESSION_DATA, null)?.let {
                Gson().fromJson(it, LocalSession::class.java)
            }
        }

    override fun getCachedPostbox(): OngoingPostboxData? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_POSTBOX_DATA, null)?.let {
                Gson().fromJson(it, OngoingPostboxData::class.java)
            }
        }

    override fun getCachedCredential(): CredentialsPayload? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, CredentialsPayload::class.java)
            }
        }
}