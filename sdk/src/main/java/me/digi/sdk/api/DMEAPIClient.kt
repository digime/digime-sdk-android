package me.digi.sdk.api

import android.content.Context
import me.digi.sdk.api.helpers.DMECertificatePinnerBuilder
import me.digi.sdk.api.helpers.DMESharedAPIScope
import me.digi.sdk.api.interceptors.DMEDefaultHeaderAppender
import me.digi.sdk.api.services.DMEArgonService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

internal class DMEAPIClient(private val context: Context, private val baseUrl: String) {

    private val httpClient: Retrofit
    val argonService: DMEArgonService
    val sharedAPIScope: DMESharedAPIScope = DMESharedAPIScope()

    init {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(DMEDefaultHeaderAppender())
            .configureCertificatePinningIfNecessary()

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())

        httpClient = retrofitBuilder.build()
        argonService = httpClient.create(DMEArgonService::class.java)
    }

    private fun OkHttpClient.Builder.configureCertificatePinningIfNecessary(): OkHttpClient.Builder {
        val certPinnerBuilder = DMECertificatePinnerBuilder(context, domainForBaseUrl())
        if (certPinnerBuilder.shouldPinCommunicationsWithDomain())
            this.certificatePinner(certPinnerBuilder.buildCertificatePinner())
        return this
    }

    private fun domainForBaseUrl() = URL(baseUrl).host
}