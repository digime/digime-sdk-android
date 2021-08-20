package me.digi.ongoingpostbox.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.DigiMe
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DataWriteResponse

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

    private val writeClient: DigiMe by lazy {

        val configuration = DigiMeConfiguration(
            context.getString(R.string.digime_application_id),
            context.getString(R.string.digime_contract_id),
            context.getString(R.string.digime_private_key)
        )

        configuration.baseUrl = "https://api.stagingdigi.me/"

        DigiMe(context, configuration)
    }

    override fun authorizeAccess(activity: Activity): Single<AuthorizationResponse> =
        Single.create { emitter ->
            writeClient.authorizeWriteAccess(
                activity,
                writeDataPayload = localDataAccess.getCachedPostbox(),
                credentials = localDataAccess.getCachedCredential()
            ) { response, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(response as AuthorizationResponse)
            }
        }

    override fun writeData(
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse> = Single.create { emitter ->
        writeClient.write(
            payload,
            accessToken
        ) { response, error ->
            error?.let(emitter::onError)
                ?: emitter.onSuccess(response as DataWriteResponse)
        }
    }
}