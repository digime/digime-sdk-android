package saas.test.app.framework.di

import saas.test.app.data.repository.DefaultMainRepository
import saas.test.app.data.repository.MainRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoriesModule: Module = module {
    single<MainRepository> { DefaultMainRepository(get(), get()) }
}