package me.digi.saas.framework.datasource

import android.content.Context
import me.digi.saas.data.localaccess.MainLocalDataAccess
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess

class MainRemoteDataAccessImpl(
    private val context: Context,
    private val localDataAccess: MainLocalDataAccess
) : MainRemoteDataAccess {
}