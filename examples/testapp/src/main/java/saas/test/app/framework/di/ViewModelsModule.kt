package saas.test.app.framework.di

import saas.test.app.features.auth.viewmodel.AuthViewModel
import saas.test.app.features.details.viewmodel.DetailsViewModel
import saas.test.app.features.home.viewmodel.HomeViewModel
import saas.test.app.features.onboard.viewmodel.OnboardViewModel
import saas.test.app.features.read.viewmodel.ReadViewModel
import saas.test.app.features.readraw.viewmodel.ReadRawViewModel
import saas.test.app.features.write.viewmodel.WriteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelsModule: Module = module {
    viewModel { AuthViewModel(get()) }
    viewModel { OnboardViewModel(get(), get()) }
    viewModel { ReadViewModel(get()) }
    viewModel { WriteViewModel(get()) }
    viewModel { ReadRawViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { DetailsViewModel(get()) }
}