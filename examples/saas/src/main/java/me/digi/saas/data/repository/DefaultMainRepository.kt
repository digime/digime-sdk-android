package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.AuthorizeResponse
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.SaasOngoingPushResponse
import me.digi.sdk.saas.serviceentities.Service

class DefaultMainRepository(
    private val remoteAccess: MainRemoteDataAccess,
    private val localAccess: MainLocalDataAccess
) : MainRepository {

    override fun authenticate(activity: Activity, contractType: String): Single<AuthorizeResponse> =
        remoteAccess.authenticate(activity, contractType)
            .compose(localAccess.cacheAuthSessionCredentials())

    override fun getFileList(): Single<DMEFileList> = remoteAccess.getFileList()

    override fun getRawFileList(): Single<DMEFileList> = remoteAccess.getRawFileList()

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> = remoteAccess.onboardService(activity, serviceId, accessToken)

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        remoteAccess.getServicesForContract(contractId)

    override fun pushDataToPostbox(payload: DMEPushPayload, accessToken: String): Single<SaasOngoingPushResponse> =
        remoteAccess.pushDataToPostbox(payload, accessToken)
}