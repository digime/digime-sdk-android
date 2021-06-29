package me.digi.ongoingpostbox.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.DMEOngoingPostboxData
import me.digi.sdk.entities.DMETokenExchange

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedCredential(): DMETokenExchange?
    fun getCachedPostbox(): DMEOngoingPostboxData?
    fun cacheCredentials(): SingleTransformer<Pair<DMEOngoingPostboxData?, DMETokenExchange?>, Pair<DMEOngoingPostboxData?, DMETokenExchange?>>
}