package me.digi.ongoingpostbox.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.ongoingpostbox.framework.utils.authorizeSaasPostbox
import me.digi.ongoingpostbox.framework.utils.pushData
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.*

/**
 * Idea behind remote main data access is to isolate
 * remote calls from local ones.
 * In which case in the repository/ies we can call one
 * or the other, or combine them to get seamless data access flow
 */
class MainRemoteDataAccessImpl(
    private val context: Context,
    private val localDataAccess: MainLocalDataAccess
) : MainRemoteDataAccess {

    private val client: DMEPushClient by lazy {

        val configuration = DMEPushConfiguration(
            context.getString(R.string.digime_application_id),
            context.getString(R.string.digime_contract_id),
            context.getString(R.string.digime_private_key)
        )

        configuration.baseUrl = "https://api.stagingdigi.me/"

        DMEPushClient(context, configuration)
    }

    override fun createPostbox(activity: Activity): Single<DMESaasOngoingPostbox?> =
        client.authorizeSaasPostbox(
            activity,
            localDataAccess.getCachedPostbox(),
            localDataAccess.getCachedCredential()
        )
            .map { it }
            .compose(localDataAccess.cacheCredentials())
            .map { it }

    override fun uploadDataToOngoingPostbox(
        pushPayload: DMEPushPayload?,
        credentials: DMETokenExchange?
    ): Single<SaasOngoingPushResponse> = client.pushData(pushPayload, credentials).map { it }
}