package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.Service

class DefaultMainRepository(
    private val remoteAccess: MainRemoteDataAccess,
    private val localAccess: MainLocalDataAccess
) : MainRepository {

    override fun getFileList(): Single<FileList> = remoteAccess.getFileList()

    override fun getRawFileList(): Single<FileList> = remoteAccess.getRawFileList()

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> = remoteAccess.onboardService(activity, serviceId, accessToken)

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        remoteAccess.getServicesForContract(contractId)

    override fun pushDataToPostbox(
        payloadWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse> =
        remoteAccess.pushDataToPostbox(payloadWrite, accessToken)

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

    override fun getFile(fileName: String): Single<FileItemBytes> =
        remoteAccess.getFile(fileName)
}