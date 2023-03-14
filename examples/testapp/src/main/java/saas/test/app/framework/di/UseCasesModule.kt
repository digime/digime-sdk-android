package saas.test.app.framework.di

import saas.test.app.usecases.*
import org.koin.core.module.Module
import org.koin.dsl.module

val useCasesModule: Module = module {
    single<AuthorizeAccessUseCase> { AuthorizeAccessUseCaseImpl(get()) }
    single<OnboardServiceUseCase> { OnboardServiceUseCaseImpl(get()) }
    single<GetFileUseCase> { GetFileUseCaseImpl(get()) }
    single<GetFileListUseCase> { GetFileListUseCaseImpl(get()) }
    single<GetAccountsUseCase> { GetAccountsUseCaseImpl(get()) }
    single<GetRawSessionDataUseCase> { GetRawSessionDataUseCaseImpl(get()) }
    single<WriteDataUseCase> { WriteDataUseCaseImpl(get()) }
    single<GetServicesForContractUseCase> { GetServicesForContractUseCaseImpl(get()) }
    single<DeleteLibraryUseCase> { DeleteLibraryUseCaseImpl(get()) }
    single<GetFileBytesUseCase> { GetFileBytesUseCaseImpl(get()) }
}