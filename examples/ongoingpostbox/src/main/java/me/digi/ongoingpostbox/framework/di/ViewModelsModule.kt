package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.features.viewmodel.MainViewModel
import org.koin.dsl.module

val viewModelsModule = module {
    single { MainViewModel(get(), get()) }
}