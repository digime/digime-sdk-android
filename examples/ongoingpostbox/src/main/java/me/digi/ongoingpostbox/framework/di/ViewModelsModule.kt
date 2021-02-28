package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.features.connect.viewmodel.CreatePostboxViewModel
import me.digi.ongoingpostbox.features.send.viewmodel.SendDataViewModel
import org.koin.dsl.module

val viewModelsModule = module {
    single { SendDataViewModel(get()) }
    single { CreatePostboxViewModel(get()) }
}