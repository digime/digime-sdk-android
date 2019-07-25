package me.digi.sdk.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response

internal class DMEDefaultHeaderAppender(): Interceptor {

    companion object {
        val defaultHeaders = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Cache-Control" to "no-cache"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val requestBuilder = request.newBuilder()

        defaultHeaders.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        return chain.proceed(request)
    }
}