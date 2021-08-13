package me.digi.saas.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.DMEFile
import me.digi.sdk.entities.response.DMEFileList
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import me.digi.sdk.entities.service.Service

interface MainRemoteDataAccess {

    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getFileList(): Single<DMEFileList>
    fun getRawFileList(): Single<DMEFileList>
    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse>

    fun deleteUsersLibrary(accessToken: String?): Single<Boolean>

    fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        credentials: CredentialsPayload?,
        serviceId: String?,
    ): Single<AuthorizationResponse>

    fun getFile(fileName: String): Single<DMEFile>
}