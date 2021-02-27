package me.digi.ongoingpostbox.usecases

import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPushPayload

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