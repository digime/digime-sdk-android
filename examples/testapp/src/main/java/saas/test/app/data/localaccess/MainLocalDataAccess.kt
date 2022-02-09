package saas.test.app.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.ConsentAuthResponse
import saas.test.app.entities.ContractHandler

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
    fun getCachedSession(): Session?
    fun getCachedPostbox(): ConsentAuthResponse?
    fun getCachedCredential(): CredentialsPayload?
}