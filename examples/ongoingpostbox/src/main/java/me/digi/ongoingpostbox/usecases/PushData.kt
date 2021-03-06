package me.digi.ongoingpostbox.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPushPayload

/**
 * Use case - Push data to Postbox
 * @see [MainRepository]
 *
 * We're isolating main flow into separate use cases (as you can see
 * we pass repository as a parameter into the use case implementation).
 * That way we can individually handle incoming data (transform it), and focus on only ONE
 * needed flow of information
 */
interface PushDataToOngoingPostboxUseCase {
    operator fun invoke(pushPayload: DMEPushPayload?, credentials: DMEOAuthToken?): Single<Boolean>
}

class PushDataToOngoingPostboxUseCaseImpl(private val repository: MainRepository) :
    PushDataToOngoingPostboxUseCase {
    override fun invoke(
        pushPayload: DMEPushPayload?,
        credentials: DMEOAuthToken?
    ): Single<Boolean> = repository.uploadDataToOngoingPostbox(pushPayload, credentials)
}