package me.digi.ongoingpostbox.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.ConsentAuthResponse

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedCredential(): CredentialsPayload?
    fun getCachedSession(): Session?
    fun getCachedPostbox(): ConsentAuthResponse?
    fun cacheAuthorizationData(): SingleTransformer<in AuthorizationResponse, out AuthorizationResponse>
}