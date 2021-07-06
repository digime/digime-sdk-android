package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.*
import me.digi.sdk.saas.serviceentities.Service

class DefaultMainRepository(
    private val remoteAccess: MainRemoteDataAccess,
    private val localAccess: MainLocalDataAccess
) : MainRepository {

    override fun authenticate(activity: Activity, contractType: String): Single<AuthSession> =
        remoteAccess.authenticate(activity, contractType)
            .map { it }
            .compose(localAccess.cacheAuthSessionCredentials())
            .map { it }

    override fun getFileList(): Single<DMEFileList> = remoteAccess.getFileList()

    override fun onboardService(
        activity: Activity,
        codeValue: String,
        serviceId: String
    ): Single<Boolean> = remoteAccess.onboardService(activity, codeValue, serviceId)

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        remoteAccess.getServicesForContract(contractId)

    override fun pushDataToPostbox(payload: DMEPushPayload, accessToken: String): Single<SaasOngoingPushResponse> =
        remoteAccess.pushDataToPostbox(payload, accessToken)
}