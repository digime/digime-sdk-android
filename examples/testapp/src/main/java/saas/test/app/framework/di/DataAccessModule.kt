package saas.test.app.framework.di

import saas.test.app.data.localaccess.MainLocalDataAccess
import saas.test.app.data.remoteaccess.MainRemoteDataAccess
import saas.test.app.framework.datasource.MainLocalDataAccessImpl
import saas.test.app.framework.datasource.MainRemoteDataAccessImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val dataAccessModule: Module = module {
    single<MainLocalDataAccess> { MainLocalDataAccessImpl(get()) }
    single<MainRemoteDataAccess> { MainRemoteDataAccessImpl(get(), get()) }
}