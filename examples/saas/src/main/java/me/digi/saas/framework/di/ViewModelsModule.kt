package me.digi.saas.framework.di

import me.digi.saas.features.auth.viewmodel.AuthViewModel
import me.digi.saas.features.onboard.viewmodel.OnboardViewModel
import me.digi.saas.features.pull.viewmodel.PullViewModel
import me.digi.saas.features.pullraw.viewmodel.PullRawViewModel
import me.digi.saas.features.push.viewmodel.PushViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelsModule: Module = module {
    viewModel { AuthViewModel(get()) }
    viewModel { OnboardViewModel(get()) }
    viewModel { PullViewModel() }
    viewModel { PushViewModel() }
    viewModel { PullRawViewModel() }
}