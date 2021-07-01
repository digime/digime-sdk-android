package me.digi.ongoingpostbox.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.DMEOngoingPostboxData
import me.digi.sdk.entities.DMESaasOngoingPostbox
import me.digi.sdk.entities.DMETokenExchange
import me.digi.sdk.entities.Session

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedCredential(): DMETokenExchange?
    fun getCachedPostbox(): DMEOngoingPostboxData?
    fun getCachedSession(): Session?
    fun cacheCredentials(): SingleTransformer<DMESaasOngoingPostbox?, DMESaasOngoingPostbox?>
}