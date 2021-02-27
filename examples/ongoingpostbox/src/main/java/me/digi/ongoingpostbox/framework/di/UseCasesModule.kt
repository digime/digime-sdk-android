package me.digi.ongoingpostbox.framework.di

import me.digi.ongoingpostbox.usecases.CreatePostboxUseCase
import me.digi.ongoingpostbox.usecases.CreatePostboxUseCaseImpl
import me.digi.ongoingpostbox.usecases.PushDataToOngoingPostboxUseCase
import me.digi.ongoingpostbox.usecases.PushDataToOngoingPostboxUseCaseImpl
import org.koin.dsl.module

val useCasesModule = module {
    single<CreatePostboxUseCase> { CreatePostboxUseCaseImpl(get()) }
    single<PushDataToOngoingPostboxUseCase> { PushDataToOngoingPostboxUseCaseImpl(get()) }
}