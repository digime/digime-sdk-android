package me.digi.saas.data.localaccess

import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.saas.entities.ContractHandler
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.entities.response.ConsentAuthResponse


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