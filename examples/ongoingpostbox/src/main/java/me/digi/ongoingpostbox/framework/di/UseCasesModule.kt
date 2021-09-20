package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.usecases.AuthorizeAccessUseCase
import me.digi.ongoingpostbox.usecases.AuthorizeAccessUseCaseImpl
import me.digi.ongoingpostbox.usecases.WriteDataUseCase
import me.digi.ongoingpostbox.usecases.WriteDataUseCaseImpl
import org.koin.dsl.module

val useCasesModule = module {
    single<AuthorizeAccessUseCase> { AuthorizeAccessUseCaseImpl(get()) }
    single<WriteDataUseCase> { WriteDataUseCaseImpl(get()) }
}