package me.digi.ongoingpostbox.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
import me.digi.ongoingpostbox.domain.OngoingPostboxResponseBody

interface CreatePostboxUseCase {
    operator fun invoke(activity: Activity): Single<OngoingPostboxResponseBody>
}

class CreatePostboxUseCaseImpl(private val repository: MainRepository) : CreatePostboxUseCase {
    override operator fun invoke(activity: Activity): Single<OngoingPostboxResponseBody> =
        repository
            .createPostbox(activity)
            .map {
                OngoingPostboxResponseBody(
                    it.first?.sessionKey,
                    it.first?.postboxId,
                    it.first?.publicKey,
                    it.first?.digiMeVersion,
                    it.second?.accessToken,
                    it.second?.expiresOn,
                    it.second?.refreshToken,
                    it.second?.tokenType
                )
            }
}