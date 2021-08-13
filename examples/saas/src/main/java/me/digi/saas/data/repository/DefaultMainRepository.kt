package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DMEFile
import me.digi.sdk.entities.response.DMEFileList
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import me.digi.sdk.entities.service.Service

class DefaultMainRepository(
    private val remoteAccess: MainRemoteDataAccess,
    private val localAccess: MainLocalDataAccess
) : MainRepository {

    override fun getFileList(): Single<DMEFileList> = remoteAccess.getFileList()

    override fun getRawFileList(): Single<DMEFileList> = remoteAccess.getRawFileList()

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> = remoteAccess.onboardService(activity, serviceId, accessToken)

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        remoteAccess.getServicesForContract(contractId)

    override fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse> =
        remoteAccess.pushDataToPostbox(payload, accessToken)

    override fun deleteUsersLibrary(): Single<Boolean> =
        remoteAccess.deleteUsersLibrary(localAccess.getCachedCredential()?.accessToken?.value)

    override fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        serviceId: String?
    ): Single<AuthorizationResponse> =
        remoteAccess
            .authorizeAccess(
                activity,
                contractType,
                scope,
                localAccess.getCachedCredential(),
                serviceId
            )
            .compose(localAccess.cacheAuthorizationData())

    override fun getFile(fileName: String): Single<DMEFile> =
        remoteAccess.getFile(fileName)
}