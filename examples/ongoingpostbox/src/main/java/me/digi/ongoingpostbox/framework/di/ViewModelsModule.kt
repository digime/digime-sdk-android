package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.features.create.viewmodel.CreatePostboxViewModel
import me.digi.ongoingpostbox.features.upload.viewmodel.UploadContentViewModel
import org.koin.dsl.module

val viewModelsModule = module {
    single { UploadContentViewModel(get()) }
    single { CreatePostboxViewModel(get()) }
}