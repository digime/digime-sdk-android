package me.digi.saas.framework.di

import me.digi.saas.usecases.*
import org.koin.core.module.Module
import org.koin.dsl.module

val useCasesModule: Module = module {
    single<AuthenticateUseCase> { AuthenticateUseCaseImpl(get()) }
    single<OnboardServiceUseCase> { OnboardServiceUseCaseImpl(get()) }
    single<GetSessionDataUseCase> { GetSessionDataUseCaseImpl(get()) }
    single<PushDataUseCase> { PushDataUseCaseImpl() }
    single<GetServicesForContractUseCase> { GetServicesForContractUseCaseImpl(get()) }
}