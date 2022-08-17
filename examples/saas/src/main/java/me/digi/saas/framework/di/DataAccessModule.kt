package me.digi.saas.framework.di

import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.saas.framework.datasource.MainLocalDataAccessImpl
import me.digi.saas.framework.datasource.MainRemoteDataAccessImpl
import org.koin.core.module.Module
import org.koin.dsl.module
import me.digi.saas.data.localaccess.MainLocalDataAccess

val dataAccessModule: Module = module {
    single<MainLocalDataAccess> { MainLocalDataAccessImpl(get()) }
    single<MainRemoteDataAccess> { MainRemoteDataAccessImpl(get(), get()) }
}