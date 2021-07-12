package me.digi.saas.framework.di

import me.digi.saas.usecases.*
import org.koin.core.module.Module
import org.koin.dsl.module

val useCasesModule: Module = module {
    single<AuthenticateUseCase> { AuthenticateUseCaseImpl(get()) }
    single<OnboardServiceUseCase> { OnboardServiceUseCaseImpl(get()) }
    single<GetSessionDataUseCase> { GetSessionDataUseCaseImpl(get()) }
    single<GetRawSessionDataUseCase> { GetRawSessionDataUseCaseImpl(get()) }
    single<PushDataUseCase> { PushDataUseCaseImpl(get()) }
    single<GetServicesForContractUseCase> { GetServicesForContractUseCaseImpl(get()) }
}