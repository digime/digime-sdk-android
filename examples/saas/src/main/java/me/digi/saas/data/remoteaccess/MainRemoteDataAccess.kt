package me.digi.saas.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.saas.serviceentities.Service

interface MainRemoteDataAccess {
    fun authenticate(activity: Activity, contractType: String) : Single<AuthSession>
    fun onboardService(activity: Activity, codeValue: String, serviceId: String): Single<Boolean>
    fun getFileList(): Single<DMEFileList>
    fun getServicesForContract(contractId: String): Single<List<Service>>
}