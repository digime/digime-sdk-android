package me.digi.saas.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.AuthorizeResponse
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.SaasOngoingPushResponse
import me.digi.sdk.entities.service.Service

interface MainRemoteDataAccess {
    fun authenticate(
        activity: Activity,
        contractType: String,
        accessToken: String?
    ): Single<AuthorizeResponse>

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
}