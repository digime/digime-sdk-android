package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.response.AuthorizeResponse
import me.digi.sdk.entities.response.DMEFileList
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.response.SaasOngoingPushResponse
import me.digi.sdk.entities.service.Service

interface MainRepository {
    fun authenticate(activity: Activity, contractType: String): Single<AuthorizeResponse>
    fun getFileList(): Single<DMEFileList>
    fun getRawFileList(): Single<DMEFileList>
    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(
        payload: DMEPushPayload,
        accessToken: String
    ): Single<SaasOngoingPushResponse>

    fun deleteUsersLibrary(): Single<Boolean>
}