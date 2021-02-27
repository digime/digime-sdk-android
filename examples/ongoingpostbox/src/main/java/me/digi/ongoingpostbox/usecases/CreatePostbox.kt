package me.digi.ongoingpostbox.usecases

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.ongoingpostbox.data.MainRepository
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox

interface CreatePostboxUseCase {
    operator fun invoke(activity: Activity): Single<Pair<DMEPostbox?, DMEOAuthToken?>>
}

class CreatePostboxUseCaseImpl(private val repository: MainRepository): CreatePostboxUseCase {
    override operator fun invoke(activity: Activity) = repository.createPostbox(activity)
}