package me.digi.saas.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.*

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedCredential(): DMETokenExchange?
    fun getCachedPostbox(): DMEOngoingPostboxData?
    fun getCachedSession(): Session?
    fun cacheAuthSessionCredentials(): SingleTransformer<AuthorizeResponse?, AuthorizeResponse?>
}