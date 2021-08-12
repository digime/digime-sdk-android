package me.digi.saas.framework.di

import me.digi.saas.features.auth.viewmodel.AuthViewModel
import me.digi.saas.features.home.viewmodel.HomeViewModel
import me.digi.saas.features.onboard.viewmodel.OnboardViewModel
import me.digi.saas.features.read.viewmodel.ReadViewModel
import me.digi.saas.features.readraw.viewmodel.ReadRawViewModel
import me.digi.saas.features.write.viewmodel.WriteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelsModule: Module = module {
    viewModel { AuthViewModel(get()) }
    viewModel { OnboardViewModel(get(), get()) }
    viewModel { ReadViewModel(get()) }
    viewModel { WriteViewModel(get()) }
    viewModel { ReadRawViewModel(get()) }
    viewModel { HomeViewModel(get()) }
}