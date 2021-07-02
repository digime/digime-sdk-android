package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList

class DefaultMainRepository(private val remoteAccess: MainRemoteDataAccess, private val localAccess: MainLocalDataAccess) : MainRepository {

    override fun authenticate(activity: Activity): Single<AuthSession> =
        remoteAccess.authenticate(activity)
            .map { it }
            .compose(localAccess.cacheAuthSessionCredentials())
            .map { it }

    override fun getFileList(): Single<DMEFileList> = remoteAccess.getFileList()
}