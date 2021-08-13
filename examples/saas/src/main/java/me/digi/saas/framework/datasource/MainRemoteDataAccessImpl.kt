package me.digi.saas.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.saas.features.utils.ContractType
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEError
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DMEFile
import me.digi.sdk.entities.response.DMEFileList
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import me.digi.sdk.entities.service.Service
import me.digi.sdk.unify.DigiMeClient
import me.digi.sdk.unify.DigiMeConfiguration

class MainRemoteDataAccessImpl(
    private val context: Context,
    private val localAccess: MainLocalDataAccess
) : MainRemoteDataAccess {

    private val readClient: DigiMeClient by lazy {

        val configuration = DigiMeConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedReadContract()?.contractId!!,
            localAccess.getCachedReadContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DigiMeClient(context, configuration)
    }

    private val writeClient: DigiMeClient by lazy {

        val configuration = DigiMeConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedPushContract()?.contractId!!,
            localAccess.getCachedPushContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DigiMeClient(context, configuration)
    }

    private val readRawClient: DigiMeClient by lazy {

        val configuration = DigiMeConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedReadRawContract()?.contractId!!,
            localAccess.getCachedReadRawContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DigiMeClient(context, configuration)
    }

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> =
        Single.create { emitter ->
            readClient.onboardService(activity, serviceId, accessToken) { error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(true)
            }
        }

    override fun getFileList(): Single<DMEFileList> = Single.create { emitter ->
        readClient.getFileList { fileList: DMEFileList?, error ->
            error?.let(emitter::onError)
                ?: (if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(DMEAuthError.General()))
        }
    }

    override fun getRawFileList(): Single<DMEFileList> = Single.create { emitter ->
        readRawClient.getFileList { fileList: DMEFileList?, error ->
            error?.let(emitter::onError)
                ?: (if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(DMEAuthError.General()))
        }
    }

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        Single.create { emitter ->
            readClient.getServicesForContractId(contractId) { servicesResponse, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(servicesResponse?.data?.services as List<Service>)
            }
        }

    override fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse> =
        Single.create { emitter ->
            writeClient.writeData(
                payload,
                accessToken
            ) { response: SaasOngoingPushResponse?, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(response as SaasOngoingPushResponse)
            }
        }

    override fun deleteUsersLibrary(accessToken: String?): Single<Boolean> =
        Single.create { emitter ->
            readClient.deleteUser(accessToken) { isLibraryDeleted, error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(isLibraryDeleted as Boolean)
            }
        }

    override fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        credentials: CredentialsPayload?,
        serviceId: String?
    ): Single<AuthorizationResponse> =
        Single.create { emitter ->
            when (contractType) {
                ContractType.pull -> readClient.authorizeAccess(
                    activity,
                    scope,
                    credentials,
                    serviceId
                ) { response, error -> handleIncomingData(response, error, emitter) }
                ContractType.push -> writeClient.authorizeAccess(
                    activity,
                    scope,
                    credentials,
                    serviceId
                ) { response, error -> handleIncomingData(response, error, emitter) }
                ContractType.readRaw -> readRawClient.authorizeAccess(
                    activity,
                    scope,
                    credentials,
                    serviceId
                ) { response, error -> handleIncomingData(response, error, emitter) }
                else -> throw IllegalArgumentException("Unknown or empty contract type")
            }
        }

    private fun handleIncomingData(
        response: AuthorizationResponse?,
        error: DMEError?,
        emitter: SingleEmitter<AuthorizationResponse>
    ) = (error?.let(emitter::onError)
        ?: (if (response != null) emitter.onSuccess(response)
        else emitter.onError(DMEAuthError.General())))

    override fun getFile(fileName: String): Single<DMEFile> =
        Single.create { emitter ->
            readClient.getFileByName(fileId = fileName, sessionKey = localAccess.getCachedAuthData()?.sessionKey!!) { file, error ->
                error?.let(emitter::onError)
                    ?: (if (file != null) emitter.onSuccess(file) else emitter.onError(DMEAuthError.General()))
            }
        }
}