package me.digi.ongoingpostbox.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.OngoingPostboxData
import me.digi.sdk.entities.OngoingPostbox
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.Session

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedCredential(): CredentialsPayload?
    fun getCachedPostbox(): OngoingPostboxData?
    fun getCachedSession(): Session?
    fun cacheCredentials(): SingleTransformer<OngoingPostbox?, OngoingPostbox?>
}