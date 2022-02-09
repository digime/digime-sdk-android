package saas.test.app.usecases

import io.reactivex.rxjava3.core.Single
import saas.test.app.data.repository.MainRepository

interface DeleteLibraryUseCase {
    operator fun invoke(): Single<Boolean>
}

class DeleteLibraryUseCaseImpl(private val repository: MainRepository) : DeleteLibraryUseCase {

    override fun invoke(): Single<Boolean> = repository.deleteUsersLibrary()
}