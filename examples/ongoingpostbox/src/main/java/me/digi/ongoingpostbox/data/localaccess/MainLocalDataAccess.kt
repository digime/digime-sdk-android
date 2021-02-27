package me.digi.ongoingpostbox.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.DMEOAuthToken
import me.digi.sdk.entities.DMEPostbox

interface MainLocalDataAccess {
    fun getCachedCredential(): DMEOAuthToken?
    fun getCachedPostbox(): DMEPostbox?
    fun cacheCredentials(): SingleTransformer<Pair<DMEPostbox?, DMEOAuthToken?>, Pair<DMEPostbox?, DMEOAuthToken?>>
}