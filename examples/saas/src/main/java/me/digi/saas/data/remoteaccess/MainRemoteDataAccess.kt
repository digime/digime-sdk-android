package me.digi.saas.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList

interface MainRemoteDataAccess {
    fun authenticate(activity: Activity) : Single<AuthSession>
    fun onboardService(activity: Activity, codeValue: String, serviceId: String): Single<Boolean>
    fun getFileList(): Single<DMEFileList>
}