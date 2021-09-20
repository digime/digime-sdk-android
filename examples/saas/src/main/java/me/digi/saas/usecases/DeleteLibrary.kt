package me.digi.saas.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.repository.MainRepository

interface DeleteLibraryUseCase {
    operator fun invoke(): Single<Boolean>
}

class DeleteLibraryUseCaseImpl(private val repository: MainRepository) : DeleteLibraryUseCase {

    override fun invoke(): Single<Boolean> = repository.deleteUsersLibrary()
}