package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.features.create.viewmodel.CreatePostboxViewModel
import me.digi.ongoingpostbox.features.upload.viewmodel.UploadDataViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { CreatePostboxViewModel(get()) }
    viewModel { UploadDataViewModel(get()) }
}