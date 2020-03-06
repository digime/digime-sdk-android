package me.digi.sdk.api.interceptors

import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.JsonWebToken
import me.digi.sdk.utilities.jwt.JwtClaim
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.waitMillis
import okio.Buffer
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class DMERetryInterceptor(private val config: DMEClientConfiguration) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // Run request.
        val request = chain.request()
        var response = chain.proceed(request)

        // Return if retry disabled.
        if (!config.retryOnFail)
            return response

        var retryAttemptsRemaining = config.maxRetryCount

        if (isRecoverableError(response)) {
            // All the while the request fails, retry it if below cap.
            while (!response.isSuccessful && retryAttemptsRemaining > 0) {

                retryAttemptsRemaining--

                val waitTime = {
                    var t = config.retryDelay.toLong()

                    if (config.retryWithExponentialBackOff) {
                        repeat(config.maxRetryCount - retryAttemptsRemaining) { t *= 2 }
                    }

                    t
                }.invoke()

                // TODO: Implement error based filtering to avoid non-recoverable retries.
                synchronized(request) {
                    request.waitMillis(waitTime)
                }

                response.close()

                val newRequest = request.withRegeneratedJwtNonce()
                response = chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun isRecoverableError(response: Response) = when (response.code) {
        400, 401 -> false
        else -> true
    }

    private fun Request.withRegeneratedJwtNonce(): Request {
        val authHeader = header("Authorization") ?: return this
        val signingKey = (config as? DMEPullConfiguration)?.privateKeyHex?.let { DMEKeyTransformer.javaPrivateKeyFromHex(it) } ?: return this
        val tokenisedJwt = authHeader.split(" ").last()
        val jwt = JsonWebToken(tokenisedJwt)

        val newNonce = DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(16))
        val newPayload = jwt.payload.toMutableMap().apply { this["nonce"] = newNonce }
        jwt.payload = newPayload

        val newTokenisedJwt = jwt.sign(signingKey).tokenize()
        val newAuthHeader = "Bearer $newTokenisedJwt"
        val newRequest = newBuilder()
            .header("Authorization", newAuthHeader)
            .build()

        return newRequest
    }
}