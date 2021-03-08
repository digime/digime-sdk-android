package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.data.DefaultMainRepository
import me.digi.ongoingpostbox.data.MainRepository
import org.koin.dsl.module

val repositoriesModule = module {
    single<MainRepository> { DefaultMainRepository(get()) }
}