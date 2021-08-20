package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileList
import me.digi.sdk.entities.response.DataWriteResponse
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
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse> =
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
                serviceId,
                localAccess.getCachedPostbox()
            )
            .compose(localAccess.cacheAuthorizationData())

    override fun getFile(fileName: String): Single<FileItem> =
        remoteAccess.getFile(fileName)
}