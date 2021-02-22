package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.framework.datasource.DigiMeService
import org.koin.dsl.module

val utilsModule = module {
    single { DigiMeService(get()) }
}