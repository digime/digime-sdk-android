package me.digi.sdk.api.interceptors

import me.digi.sdk.entities.DMEClientConfiguration
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.waitMillis

class DMERetryInterceptor(private val config: DMEClientConfiguration): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // Run request.
        val request = chain.request()
        var response = chain.proceed(request)

        // Return if retry disabled.
        if (!config.retryOnFail)
            return response

        var retryAttemptsRemaining = config.maxRetryCount

        // All the while the request fails, retry it if below cap.
        while (!response.isSuccessful && retryAttemptsRemaining > 0) {

            retryAttemptsRemaining--
            // TODO: Implement error based filtering to avoid non-recoverable retries.
            waitMillis(config.retryDelay.toLong())
            response = chain.proceed(request)

        }

        return response
    }
}