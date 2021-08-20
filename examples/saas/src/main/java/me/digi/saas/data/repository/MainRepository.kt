package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.DataPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.FileList
import me.digi.sdk.entities.response.DataWriteResponse
import me.digi.sdk.entities.service.Service

interface MainRepository {
    fun getFileList(): Single<FileList>
    fun getRawFileList(): Single<FileList>
    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(
        payload: DataPayload,
        accessToken: String
    ): Single<DataWriteResponse>

    fun deleteUsersLibrary(): Single<Boolean>

    fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        serviceId: String?,
    ): Single<AuthorizationResponse>

    fun getFile(fileName: String): Single<FileItem>
}