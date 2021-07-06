package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.SaasOngoingPushResponse
import me.digi.sdk.saas.serviceentities.Service

interface MainRepository {
    fun authenticate(activity: Activity, contractType: String) : Single<AuthSession>
    fun getFileList(): Single<DMEFileList>
    fun onboardService(activity: Activity, codeValue: String, serviceId: String): Single<Boolean>
    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(payload: DMEPushPayload, accessToken: String): Single<SaasOngoingPushResponse>
}