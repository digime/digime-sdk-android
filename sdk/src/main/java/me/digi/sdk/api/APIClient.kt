package me.digi.sdk.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.APIError
import me.digi.sdk.ArgonCode
import me.digi.sdk.Error
import me.digi.sdk.api.adapters.FileUnpackAdapter
import me.digi.sdk.api.adapters.SessionRequestAdapter
import me.digi.sdk.api.helpers.CertificatePinnerBuilder
import me.digi.sdk.api.interceptors.DefaultHeaderAppender
import me.digi.sdk.api.interceptors.RetryInterceptor
import me.digi.sdk.api.services.ArgonService
import me.digi.sdk.entities.configuration.ClientConfiguration
import me.digi.sdk.entities.configuration.ReadConfiguration
import me.digi.sdk.entities.request.SessionRequest
import me.digi.sdk.entities.response.File
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class APIClient(private val context: Context, private val clientConfig: ClientConfiguration) {

    private val httpClient: Retrofit
    internal val argonService: ArgonService

    companion object {

        fun parseError(
            argonErrorCode: String?,
            argonErrorMessage: String?,
            argonErrorReference: String?
        ) =
            APIError::class.sealedSubclasses.fold<KClass<out APIError>, APIError?>(null) { _, err ->
                val argonCode =
                    (err.annotations.firstOrNull { it is ArgonCode } as? ArgonCode)?.value
                if (argonCode == argonErrorCode) run {
                    val instance = err.createInstance()
                    instance.apply {
                        code = argonErrorCode
                        if (argonErrorMessage != null) message = argonErrorMessage
                        reference = argonErrorReference
                    }

                    return@fold instance

                } else null
            } ?: argonErrorMessage?.let {
                APIError.UNMAPPED(
                    argonErrorCode,
                    it,
                    argonErrorReference
                )
            } ?: APIError.GENERIC(argonErrorCode?.toInt(), argonErrorMessage)
    }

    init {
        val gsonBuilder = GsonBuilder()
            .registerTypeAdapter(SessionRequest::class.java, SessionRequestAdapter)
            .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): Date {
                    return Date(json?.asLong ?: 0)
                }
            })
        if (clientConfig is ReadConfiguration) {
            gsonBuilder.registerTypeAdapter(
                File::class.java,
                FileUnpackAdapter(clientConfig.privateKeyHex)
            )
        }

        val requestDispatcher = Dispatcher()
        requestDispatcher.maxRequests = clientConfig.maxConcurrentRequests

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC

        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(DefaultHeaderAppender())
            .addInterceptor(RetryInterceptor(clientConfig))
            .addInterceptor(logging)
            .configureCertificatePinningIfNecessary()
            .callTimeout(clientConfig.globalTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(clientConfig.globalTimeout.toLong(), TimeUnit.SECONDS)
            .dispatcher(requestDispatcher)

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(clientConfig.baseUrl)
            .client(httpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())


        httpClient = retrofitBuilder.build()
        argonService = httpClient.create(ArgonService::class.java)
    }

    private fun OkHttpClient.Builder.configureCertificatePinningIfNecessary(): OkHttpClient.Builder {
        val certPinnerBuilder = CertificatePinnerBuilder(context, domainForBaseUrl())
        if (certPinnerBuilder.shouldPinCommunicationsWithDomain())
            this.certificatePinner(certPinnerBuilder.buildCertificatePinner())
        return this
    }

    private fun domainForBaseUrl() = URL(clientConfig.baseUrl).host

    // Rx Overload
    fun <ResponseType> makeCall(call: Call<ResponseType>): Single<ResponseType> =
        Single.create { emitter ->
            makeCall(call) { value, error ->
                when {
                    error != null -> emitter.onError(error)
                    value != null -> emitter.onSuccess(value)
                    else -> emitter.onError(
                        APIError.GENERIC(
                            400,
                            "Something went wrong with your request"
                        )
                    )
                }
            }
        }

    fun <ResponseType> makeCall(
        call: Call<ResponseType>,
        completion: (ResponseType?, Error?) -> Unit
    ) {
        call.enqueue(object : Callback<ResponseType> {
            override fun onResponse(call: Call<ResponseType>, response: Response<ResponseType>) {

                val resultObject = response.body()

                if (resultObject != null) {
                    // Successful request - return result and no error.
                    completion(resultObject, null)
                } else {
                    val deducedError = deduceErrorFromResponse(response)
                    completion(null, deducedError)
                }
            }

            override fun onFailure(call: Call<ResponseType>, error: Throwable) {
                println("Error: ${error.localizedMessage}")
                // A failure here indicates that the API was unreachable, so we can return a generic error at best.
                val genericAPIError = APIError.UNREACHABLE()
                completion(null, genericAPIError)
            }
        })
    }

    private fun <ResponseType> deduceErrorFromResponse(response: Response<ResponseType>): Error {

        val headers = response.headers()

        val argonErrorCode = headers["X-Error-Code"] ?: run {
            return APIError.GENERIC(
                response.code(),
                response.message()
            )
        }
        val argonErrorMessage = headers["X-Error-Message"]
        val argonErrorReference = headers["X-Error-Reference"]

        return parseError(argonErrorCode, argonErrorMessage, argonErrorReference)
    }
}