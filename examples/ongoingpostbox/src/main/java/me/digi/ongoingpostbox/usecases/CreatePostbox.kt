package me.digi.ongoingpostbox.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
import me.digi.ongoingpostbox.domain.OngoingPostboxPayload
import me.digi.ongoingpostbox.domain.OngoingPostboxResponseBody

/**
 * Use case - Create Postbox
 * @see [MainRepository]
 *
 * We're isolating main flow into separate use cases (as you can see
 * we pass repository as a parameter into the use case implementation).
 * That way we can individually handle incoming data (transform it), and focus on only ONE
 * needed flow of information
 */
interface CreatePostboxUseCase {
    operator fun invoke(activity: Activity): Single<OngoingPostboxPayload>
}

/**
 * For our convenience we transform incoming data
 * to a bit more readable class (mainly to avoid nullability)
 * @see OngoingPostboxResponseBody
 */
class CreatePostboxUseCaseImpl(private val repository: MainRepository) : CreatePostboxUseCase {
    override operator fun invoke(activity: Activity): Single<OngoingPostboxPayload> =
        repository
            .createPostbox(activity)
            .map {
                OngoingPostboxPayload(
                    it.first?.postboxId,
                    it.first?.publicKey,
                    it.second?.accessToken?.value,
                    it.second?.refreshToken?.value
                )
            }
}