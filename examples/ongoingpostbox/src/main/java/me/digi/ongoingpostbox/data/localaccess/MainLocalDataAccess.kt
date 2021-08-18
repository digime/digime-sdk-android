package me.digi.ongoingpostbox.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.ongoingpostbox.domain.LocalSession
import me.digi.sdk.entities.OngoingWriteData
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedCredential(): CredentialsPayload?
    fun getCachedSession(): LocalSession?
    fun getCachedPostbox(): OngoingWriteData?
    fun cacheAuthorizationData(): SingleTransformer<in AuthorizationResponse, out AuthorizationResponse>
}