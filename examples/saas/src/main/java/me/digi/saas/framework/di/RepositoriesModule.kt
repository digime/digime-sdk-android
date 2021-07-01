package me.digi.saas.framework.di

import me.digi.saas.data.repository.DefaultMainRepository
import me.digi.saas.data.repository.MainRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoriesModule: Module = module {
    single<MainRepository> { DefaultMainRepository(get()) }
}