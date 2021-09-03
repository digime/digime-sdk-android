package me.digi.ongoingpostbox.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.R
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.Init
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.configuration.DigiMeConfiguration
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

    private val writeClient: Init by lazy {

        val configuration = DigiMeConfiguration(
            context.getString(R.string.digime_application_id),
            context.getString(R.string.digime_contract_id),
            context.getString(R.string.digime_private_key),
            "https://api.stagingdigi.me/"
        )

        Init(context, configuration)
    }

    override fun authorizeAccess(activity: Activity): Single<AuthorizationResponse> =
        Single.create { emitter ->
            writeClient.authorizeAccess(
                activity,
                credentials = localDataAccess.getCachedCredential()
            ) { response, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(response as AuthorizationResponse)
            }
        }

    override fun writeData(
        payloadWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse> = Single.create { emitter ->
        writeClient.write(
            payloadWrite,
            accessToken
        ) { response, error ->
            error?.let(emitter::onError)
                ?: emitter.onSuccess(response as DataWriteResponse)
        }
    }

    override fun updateSession(): Single<Boolean> = Single.create { emitter ->
        writeClient.updateSession { isSessionUpdated: Boolean?, error: me.digi.sdk.Error? ->
            error?.let(emitter::onError) ?: emitter.onSuccess(isSessionUpdated as @NonNull Boolean)
        }
    }
}