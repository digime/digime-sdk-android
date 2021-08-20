package me.digi.saas.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.saas.entities.ContractHandler
import me.digi.saas.entities.LocalSession
import me.digi.saas.framework.datasource.MainLocalDataAccessImpl
import me.digi.sdk.entities.WriteDataInfoPayload
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse

/**
 * Check [MainLocalDataAccessImpl] for more information
 */
interface MainLocalDataAccess {
    fun getCachedBaseUrl(): String?
    fun getCachedAppId(): String?
    fun getCachedReadContract(): ContractHandler?
    fun getCachedPushContract(): ContractHandler?
    fun getCachedReadRawContract(): ContractHandler?
    fun cacheAuthorizationData(): SingleTransformer<in AuthorizationResponse, out AuthorizationResponse>
    fun getCachedSession(): LocalSession?
    fun getCachedPostbox(): WriteDataInfoPayload?
    fun getCachedCredential(): CredentialsPayload?
}