package me.digi.sdk.api

import android.content.Context
import com.google.gson.*
import io.reactivex.Observable
import io.reactivex.Single
import me.digi.sdk.DMEAPIError
import me.digi.sdk.DMEError
import me.digi.sdk.api.adapters.DMEFileUnpackAdapter
import me.digi.sdk.api.adapters.DMESessionRequestAdapter
import me.digi.sdk.api.helpers.DMECertificatePinnerBuilder
import me.digi.sdk.api.interceptors.DMEDefaultHeaderAppender
import me.digi.sdk.api.interceptors.DMERetryInterceptor
import me.digi.sdk.api.services.DMEArgonService
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.api.DMEJsonWebToken
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class DMEAPIClient(private val context: Context, private val clientConfig: DMEClientConfiguration) {

    private val httpClient: Retrofit
    internal val argonService: DMEArgonService

    init {
        val gsonBuilder = GsonBuilder()
            .registerTypeAdapter(DMESessionRequest::class.java, DMESessionRequestAdapter)
            .registerTypeAdapter(Date::class.java, object: JsonDeserializer<Date> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): Date {
                    return Date(json?.asLong ?: 0)
                }
            })
        if (clientConfig is DMEPullConfiguration) {
            gsonBuilder.registerTypeAdapter(DMEFile::class.java, DMEFileUnpackAdapter(clientConfig.privateKeyHex))
            gsonBuilder.registerTypeAdapter(DMEJsonWebToken::class.java, DMEJsonWebToken.Adapter(DMEKeyTransformer.javaPrivateKeyFromHex(clientConfig.privateKeyHex)))
        }

        val requestDispatcher = Dispatcher()
        requestDispatcher.maxRequests = clientConfig.maxConcurrentRequests

        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(DMEDefaultHeaderAppender())
            .addInterceptor(DMERetryInterceptor(clientConfig))
            .configureCertificatePinningIfNecessary()
            .callTimeout(clientConfig.globalTimeout.toLong(), TimeUnit.SECONDS)
            .dispatcher(requestDispatcher)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(clientConfig.baseUrl)
            .client(httpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))

        httpClient = retrofitBuilder.build()
        argonService = httpClient.create(DMEArgonService::class.java)
    }

    private fun OkHttpClient.Builder.configureCertificatePinningIfNecessary(): OkHttpClient.Builder {
        val certPinnerBuilder = DMECertificatePinnerBuilder(context, domainForBaseUrl())
        if (certPinnerBuilder.shouldPinCommunicationsWithDomain())
            this.certificatePinner(certPinnerBuilder.buildCertificatePinner())
        return this
    }

    private fun domainForBaseUrl() = URL(clientConfig.baseUrl).host

    // Rx Overload
    fun <ResponseType> makeCall(call: Call<ResponseType>) = Single.create<ResponseType> { emitter ->
        makeCall(call) { value, error ->
            when {
                error != null -> emitter.onError(error)
                value != null -> emitter.onSuccess(value)
                else -> emitter.onError(DMEAPIError.Generic())
            }
        }
    }

    fun <ResponseType> makeCall(call: Call<ResponseType>, completion: (ResponseType?, DMEError?) -> Unit) {
        call.enqueue(object: Callback<ResponseType> {
            override fun onResponse(call: Call<ResponseType>, response: Response<ResponseType>) {

                val resultObject = response.body()

                if (resultObject != null) {
                    // Successful request - return result and no error.
                    completion(resultObject, null)
                }
                else {
                    val deducedError = deduceErrorFromResponse(response)
                    completion(null, deducedError)
                }
            }

            override fun onFailure(call: Call<ResponseType>, error: Throwable) {
                // A failure here indicates that the API was unreachable, so we can return a generic error at best.
                val genericAPIError = DMEAPIError.Unreachable()
                completion(null, genericAPIError)
            }
        })
    }

    private fun <ResponseType> deduceErrorFromResponse(response: Response<ResponseType>): DMEError {

        // Try to parse a digi.me error object from the response.
        val responseHeaders = response.headers().toMap()
        val digiErrorCode = responseHeaders["X-Error-Code"]
        val digiErrorMessage = responseHeaders["X-Error-Message"]
        val digiErrorReference = responseHeaders["X-Error-Reference"]

        return if (digiErrorMessage != null)
            DMEAPIError.Server(digiErrorMessage, digiErrorReference, digiErrorCode)
        else
            DMEAPIError.Generic()
    }
}