package me.digi.saas.framework.di

import me.digi.saas.usecases.*
import org.koin.core.module.Module
import org.koin.dsl.module

val useCasesModule: Module = module {
    single<AuthenticateUseCase> { AuthenticateUseCaseImpl() }
    single<OnboardServiceUseCase> { OnboardServiceUseCaseImpl() }
    single<GetSessionDataUseCase> { GetSessionDataUseCaseImpl() }
    single<PushDataUseCase> { PushDataUseCaseImpl() }
}