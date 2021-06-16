package me.digi.saas.framework.di

import me.digi.saas.features.auth.viewmodel.AuthViewModel
import me.digi.saas.features.onboard.viewmodel.OnboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { OnboardViewModel(get()) }
}