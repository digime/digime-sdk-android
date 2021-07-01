package me.digi.saas.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.AuthSession

interface MainRepository {
    fun authenticate(activity: Activity) : Single<AuthSession>
}