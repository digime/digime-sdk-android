package me.digi.sdk.api

import android.content.Context
import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.digi.sdk.DMEAPIError
import me.digi.sdk.DMEError
import me.digi.sdk.R
import me.digi.sdk.api.adapters.DMESessionRequestAdapter
import me.digi.sdk.api.helpers.DMECertificatePinnerBuilder
import me.digi.sdk.api.interceptors.DMEDefaultHeaderAppender
import me.digi.sdk.api.services.DMEArgonService
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.entities.api.DMESessionRequest
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.URL
import java.util.*
import kotlin.reflect.typeOf

class DMEAPIClient(private val context: Context, private val clientConfig: DMEClientConfiguration) {

    private val httpClient: Retrofit
    val argonService: DMEArgonService

    init {
        val gson = GsonBuilder()
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
            .create()

        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(DMEDefaultHeaderAppender())
            .configureCertificatePinningIfNecessary()

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(clientConfig.baseUrl)
            .client(httpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gson))

        httpClient = retrofitBuilder.build()
        argonService = httpClient.create(DMEArgonService::class.java)
    }

    private fun OkHttpClient.Builder.configureCertificatePinningIfNecessary(): OkHttpClient.Builder {
//        val certPinnerBuilder = DMECertificatePinnerBuilder(context, domainForBaseUrl())
//        if (certPinnerBuilder.shouldPinCommunicationsWithDomain())
//            this.certificatePinner(certPinnerBuilder.buildCertificatePinner())
        return this
    }

    private fun domainForBaseUrl() = URL(clientConfig.baseUrl).host

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
                val genericAPIError = DMEAPIError.Generic(error.message ?: "")
                completion(null, genericAPIError)
            }
        })
    }

    private fun <ResponseType> deduceErrorFromResponse(response: Response<ResponseType>): DMEError {

        // For now return generic error until I have the spec from CCS.
        // TODO: Implement full error deduction.
        return DMEAPIError.Generic("${response.message()}")

        // Try to parse a digi.me error object from the response.
//        val responseString = response.errorBody()?.string()
//        if (responseString != null) {
//            // Try and Gson it.
//            val responseJSON: Map<String, Any> = Gson().fromJson(responseString, object: TypeToken<Map<String, Any>>() {}.type)
//        }
    }
}