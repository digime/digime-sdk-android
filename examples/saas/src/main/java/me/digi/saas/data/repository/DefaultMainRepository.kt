package me.digi.saas.data.repository

import me.digi.saas.data.remoteaccess.MainRemoteDataAccess

class DefaultMainRepository(private val remoteAccess: MainRemoteDataAccess) : MainRepository {
}