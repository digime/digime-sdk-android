package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.usecases.*
import org.koin.dsl.module

val useCasesModule = module {
    single<AuthorizeAccessUseCase> { AuthorizeAccessUseCaseImpl(get()) }
    single<WriteDataUseCase> { WriteDataUseCaseImpl(get()) }
    single<UpdateSessionUseCase> { UpdateSessionUseCaseImpl(get()) }
}