package me.digi.saas.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.WriteDataInfoPayload
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileList
import me.digi.sdk.entities.response.DataWriteResponse
import me.digi.sdk.entities.service.Service

interface MainRemoteDataAccess {

    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getFileList(): Single<FileList>
    fun getRawFileList(): Single<FileList>
    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse>

    fun deleteUsersLibrary(accessToken: String?): Single<Boolean>

    fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        credentials: CredentialsPayload?,
        serviceId: String?,
        writeDataPayload: WriteDataInfoPayload?
    ): Single<AuthorizationResponse>

    fun getFile(fileName: String): Single<FileItem>
}