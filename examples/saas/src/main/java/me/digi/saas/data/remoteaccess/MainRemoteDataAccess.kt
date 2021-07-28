package me.digi.saas.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.*
import me.digi.sdk.saas.serviceentities.Service

interface MainRemoteDataAccess {
    fun authenticate(activity: Activity, contractType: String, credentials: DMETokenExchange? = null): Single<AuthorizeResponse>
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
}