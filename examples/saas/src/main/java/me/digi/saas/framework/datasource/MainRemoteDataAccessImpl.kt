package me.digi.saas.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.saas.features.utils.ContractType
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.configuration.ReadConfiguration
import me.digi.sdk.entities.configuration.WriteConfiguration
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.AuthorizeResponse
import me.digi.sdk.entities.response.DMEFileList
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import me.digi.sdk.entities.service.Service

class MainRemoteDataAccessImpl(
    private val context: Context,
    private val localAccess: MainLocalDataAccess
) : MainRemoteDataAccess {

    private val pullClient: DMEPullClient by lazy {

        val configuration = ReadConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedReadContract()?.contractId!!,
            localAccess.getCachedReadContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DMEPullClient(context, configuration)
    }

    private val pushClient: DMEPushClient by lazy {

        val configuration = WriteConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedPushContract()?.contractId!!,
            localAccess.getCachedPushContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DMEPushClient(context, configuration)
    }

    private val readRawClient: DMEPullClient by lazy {

        val configuration = ReadConfiguration(
            localAccess.getCachedAppId()!!,
            localAccess.getCachedReadRawContract()?.contractId!!,
            localAccess.getCachedReadRawContract()?.privateKeyHex!!.replace("\\n", "\n")
        )

        configuration.baseUrl = localAccess.getCachedBaseUrl()!!

        DMEPullClient(context, configuration)
    }

    override fun authenticate(
        activity: Activity,
        contractType: String,
        accessToken: String?
    ): Single<AuthorizeResponse> =
        Single.create { emitter ->
            when (contractType) {
                ContractType.pull -> pullClient.authorize(
                    activity,
                    accessToken
                ) { authResponse, error ->
                    error?.let(emitter::onError)
                        ?: (if (authResponse != null) emitter.onSuccess(authResponse)
                        else emitter.onError(DMEAuthError.General()))
                }
                ContractType.push -> pushClient.authorize(
                    activity,
                    accessToken
                ) { authResponse, error ->
                    error?.let(emitter::onError)
                        ?: (if (authResponse != null) emitter.onSuccess(authResponse)
                        else emitter.onError(DMEAuthError.General()))
                }
                ContractType.readRaw -> readRawClient.authorize(
                    activity,
                    accessToken
                ) { authResponse, error ->
                    error?.let(emitter::onError)
                        ?: (if (authResponse != null) emitter.onSuccess(authResponse)
                        else emitter.onError(DMEAuthError.General()))
                }
                else -> throw IllegalArgumentException("Unknown or empty contract type")
            }
        }

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> =
        Single.create { emitter ->
            pullClient.onboardService(activity, serviceId, accessToken) { error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(true)
            }
        }

    override fun getFileList(): Single<DMEFileList> = Single.create { emitter ->
        pullClient.getFileList { fileList: DMEFileList?, error ->
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
            pullClient.getServicesForContractId(contractId) { servicesResponse, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(servicesResponse?.data?.services as List<Service>)
            }
        }

    override fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse> =
        Single.create { emitter ->
            pushClient.pushData(payload, accessToken) { response: SaasOngoingPushResponse?, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(response as SaasOngoingPushResponse)
            }
        }

    override fun deleteUsersLibrary(accessToken: String?): Single<Boolean> =
        Single.create { emitter ->
            pullClient.deleteUser(accessToken) { isLibraryDeleted, error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(isLibraryDeleted as Boolean)
            }
        }
}