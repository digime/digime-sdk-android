package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.data.remoteaccess.MainRemoteDataAccess
import me.digi.ongoingpostbox.framework.datasource.MainLocalDataAccessImpl
import me.digi.ongoingpostbox.framework.datasource.MainRemoteDataAccessImpl
import org.koin.dsl.module

val dataAccess = module {
    single<MainLocalDataAccess> { MainLocalDataAccessImpl(get()) }
    single<MainRemoteDataAccess> { MainRemoteDataAccessImpl(get(), get()) }
}