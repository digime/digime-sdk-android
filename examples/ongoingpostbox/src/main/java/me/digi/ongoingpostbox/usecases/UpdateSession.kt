package me.digi.ongoingpostbox.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository

interface UpdateSessionUseCase {
    operator fun invoke(): Single<Boolean>
}

class UpdateSessionUseCaseImpl(private val repository: MainRepository): UpdateSessionUseCase {

    override fun invoke(): Single<Boolean> = repository.updateSession()
}