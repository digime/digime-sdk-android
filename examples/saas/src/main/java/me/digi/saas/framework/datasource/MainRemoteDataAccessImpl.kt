package me.digi.saas.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import me.digi.saas.R
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.saas.features.utils.ContractType
import me.digi.sdk.AuthError
import me.digi.sdk.Error
import me.digi.sdk.Init
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.Service
import me.digi.saas.data.localaccess.MainLocalDataAccess

class MainRemoteDataAccessImpl(
    private val context: Context,
    private val localAccess: MainLocalDataAccess
) : MainRemoteDataAccess {

    private val readClient: Init by lazy {

        val configuration = DigiMeConfiguration(
            context.resources.getString(R.string.appId),
            context.resources.getString(R.string.readContractId),
            context.resources.getString(R.string.readPrivateKey)
        )

        Init(context, configuration)
    }

    private val writeClient: Init by lazy {

        val configuration = DigiMeConfiguration(
            context.resources.getString(R.string.appId),
            context.resources.getString(R.string.writeContractId),
            context.resources.getString(R.string.writePrivateKey),
            "https://api.digi.me/"
        )

        Init(context, configuration)
    }

    private val readRawClient: Init by lazy {

        val configuration = DigiMeConfiguration(
            context.resources.getString(R.string.appId),
            context.resources.getString(R.string.readRawContractId),
            context.resources.getString(R.string.readRawPrivateKey),
            "https://api.digi.me/"
        )

        Init(context, configuration)
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
        readClient.readFileList(localAccess.getCachedCredential()?.accessToken?.value!!) { fileList: FileList?, error ->
            error?.let(emitter::onError)
                ?: (if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(AuthError.General()))
        }
    }

    override fun getRawFileList(): Single<FileList> = Single.create { emitter ->
        readRawClient.readFileList(localAccess.getCachedCredential()?.accessToken?.value!!) { fileList: FileList?, error ->
            error?.let(emitter::onError)
                ?: (if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(AuthError.General()))
        }
    }

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        Single.create { emitter ->
            readClient.getAvailableServices(null) { servicesResponse, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(servicesResponse?.data?.services as List<Service>)
            }
        }

//    override fun pushDataToPostbox(
//        payloadToWrite: WriteDataPayload,
//        accessToken: String
//    ): Single<DataWriteResponse> =
//        Single.create { emitter ->
//            writeClient.write(
//                payloadToWrite,
//                accessToken
//            ) { response: DataWriteResponse?, error ->
//                error?.let(emitter::onError)
//                    ?: emitter.onSuccess(response as DataWriteResponse)
//            }
//        }

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
                    fromActivity = activity,
                    scope = scope,
                    credentials = credentials,
                    serviceId = serviceId
                ) { response, error -> handleIncomingData(response, error, emitter) }
                ContractType.push -> writeClient.authorizeAccess(
                    fromActivity = activity,
                    credentials = credentials
                ) { response, error -> handleIncomingData(response, error, emitter) }
                ContractType.readRaw -> readRawClient.authorizeAccess(
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

    override fun getFile(fileName: String): Single<FileItemBytes> =
        Single.create { emitter ->
            readClient.readFile(
                userAccessToken = localAccess.getCachedCredential()?.accessToken?.value!!,
                fileId = fileName,
            ) { file, error ->
                error?.let(emitter::onError)
                    ?: (if (file != null) emitter.onSuccess(file) else emitter.onError(AuthError.General()))
            }
        }

    override fun getFileBytes(fileName: String): Single<FileItemBytes> =
        Single.create { emitter ->
            readRawClient.readFile(
                userAccessToken = localAccess.getCachedCredential()?.accessToken?.value!!,
                fileId = fileName,
            ) { file, error ->
                error?.let(emitter::onError)
                    ?: (if (file != null) emitter.onSuccess(file) else emitter.onError(AuthError.General()))
            }
        }
}