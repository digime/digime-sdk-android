package me.digi.saas.framework.di

import me.digi.saas.features.auth.viewmodel.AuthViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { AuthViewModel(get()) }
}