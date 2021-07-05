package me.digi.saas.framework.datasource

import android.app.Activity
import android.content.Context
import io.reactivex.rxjava3.core.Single
import me.digi.saas.R
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.saas.features.utils.ContractType
import me.digi.saas.framework.utils.AppConst
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.DMEPushConfiguration
import me.digi.sdk.saas.serviceentities.Service

class MainRemoteDataAccessImpl(private val context: Context) : MainRemoteDataAccess {

    private val pullClient: DMEPullClient by lazy {

        val configuration = DMEPullConfiguration(
            context.getString(R.string.appId),
            context.getString(R.string.pullContractId),
            context.getString(R.string.pullContractPrivateKey)
        )

        configuration.baseUrl = AppConst.BASE_URL

        DMEPullClient(context, configuration)
    }

    val pushClient: DMEPushClient by lazy {

        val configuration = DMEPushConfiguration(
            context.getString(R.string.appId),
            context.getString(R.string.pushContractId),
            context.getString(R.string.pushContractPrivateKey)
        )

        configuration.baseUrl = AppConst.BASE_URL

        DMEPushClient(context, configuration)
    }

    override fun authenticate(activity: Activity, contractType: String): Single<AuthSession> =
        Single.create { emitter ->
            when (contractType) {
                ContractType.pull -> pullClient.authorizeNew(activity) { authSession, error ->
                    error?.let(emitter::onError)
                        ?: (if (authSession != null) emitter.onSuccess(authSession)
                        else emitter.onError(DMEAuthError.General()))
                }
                ContractType.push -> {
                }
                else -> throw IllegalArgumentException("Unknown or empty contract type")
            }

        }

    override fun onboardService(
        activity: Activity,
        codeValue: String,
        serviceId: String
    ): Single<Boolean> =
        Single.create { emitter ->
            pullClient.onboardNew(activity, codeValue, serviceId) { error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(true)
            }
        }

    override fun getFileList(): Single<DMEFileList> = Single.create { emitter ->
        pullClient.getFileList { fileList: DMEFileList?, error ->
            error?.let(emitter::onError)
                ?: if (fileList != null) emitter.onSuccess(fileList)
                else emitter.onError(DMEAuthError.General())
        }
    }

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        Single.create { emitter ->
            pullClient.getServicesForContractId(contractId) { servicesResponse, error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(servicesResponse?.data?.services)
            }
        }
}