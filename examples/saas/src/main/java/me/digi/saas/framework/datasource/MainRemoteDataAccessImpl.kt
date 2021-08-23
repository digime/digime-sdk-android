package me.digi.saas.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.saas.features.utils.ContractType
import me.digi.sdk.AuthError
import me.digi.sdk.DigiMe
import me.digi.sdk.Error
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.WriteDataInfo
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DataWriteResponse
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileList
import me.digi.sdk.entities.service.Service

class MainRemoteDataAccessImpl(
    private val context: Context,
    private val localAccess: MainLocalDataAccess
) : MainRemoteDataAccess {

    private val readClient: DigiMe by lazy {

        val configuration = DigiMeConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedReadContract()?.contractId!!,
            localAccess.getCachedReadContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DigiMe(context, configuration)
    }

    private val writeClient: DigiMe by lazy {

        val configuration = DigiMeConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedPushContract()?.contractId!!,
            localAccess.getCachedPushContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DigiMe(context, configuration)
    }

    private val readRawClient: DigiMe by lazy {

        val configuration = DigiMeConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedReadRawContract()?.contractId!!,
            localAccess.getCachedReadRawContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DigiMe(context, configuration)
    }

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> =
        Single.create { emitter ->
            readClient.addService(activity, serviceId, accessToken) { error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(true)
            }
        }

    override fun getFileList(): Single<FileList> = Single.create { emitter ->
        readClient.getFileList { fileList: FileList?, error ->
            error?.let(emitter::onError)
                ?: (if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(AuthError.General()))
        }
    }

    override fun getRawFileList(): Single<FileList> = Single.create { emitter ->
        readRawClient.getFileList { fileList: FileList?, error ->
            error?.let(emitter::onError)
                ?: (if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(AuthError.General()))
        }
    }

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        Single.create { emitter ->
            readClient.availableServices { servicesResponse, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(servicesResponse?.data?.services as List<Service>)
            }
        }

    override fun pushDataToPostbox(
        payloadWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse> =
        Single.create { emitter ->
            writeClient.write(
                payloadWrite,
                accessToken
            ) { response: DataWriteResponse?, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(response as DataWriteResponse)
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
        serviceId: String?,
        writeData: WriteDataInfo?
    ): Single<AuthorizationResponse> =
        Single.create { emitter ->
            when (contractType) {
                ContractType.pull -> readClient.authorizeReadAccess(
                    fromActivity = activity,
                    scope = scope,
                    credentials = credentials,
                    serviceId = serviceId
                ) { response, error -> handleIncomingData(response, error, emitter) }
                ContractType.push -> writeClient.authorizeWriteAccess(
                    fromActivity = activity,
                    credentials = credentials,
                    data = writeData
                ) { response, error -> handleIncomingData(response, error, emitter) }
                ContractType.readRaw -> readRawClient.authorizeReadAccess(
                    fromActivity = activity,
                    scope = scope,
                    credentials = credentials,
                    serviceId = serviceId
                ) { response, error -> handleIncomingData(response, error, emitter) }
                else -> throw IllegalArgumentException("Unknown or empty contract type")
            }
        }

    private fun handleIncomingData(
        response: AuthorizationResponse?,
        error: Error?,
        emitter: SingleEmitter<AuthorizationResponse>
    ) = (error?.let(emitter::onError)
        ?: (if (response != null) emitter.onSuccess(response)
        else emitter.onError(AuthError.General())))

    override fun getFile(fileName: String): Single<FileItem> =
        Single.create { emitter ->
            readClient.getFileByName(fileId = fileName) { file, error ->
                error?.let(emitter::onError)
                    ?: (if (file != null) emitter.onSuccess(file) else emitter.onError(AuthError.General()))
            }
        }
}