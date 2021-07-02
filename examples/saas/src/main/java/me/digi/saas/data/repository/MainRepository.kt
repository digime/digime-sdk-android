package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList

interface MainRepository {
    fun authenticate(activity: Activity) : Single<AuthSession>
    fun getFileList(): Single<DMEFileList>
}