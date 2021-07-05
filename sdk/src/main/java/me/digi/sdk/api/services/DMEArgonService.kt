package me.digi.sdk.api.services

import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.saas.serviceentities.ServicesResponse
import me.digi.sdk.utilities.jwt.DMEAuthCodeExchangeResponseJWT
import me.digi.sdk.utilities.jwt.DMEPreAuthorizationResponse
import me.digi.sdk.utilities.jwt.DMEPreauthorizationResponseJWT
import me.digi.sdk.utilities.jwt.ExchangeTokenJWT
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface DMEArgonService {

    @POST("v1.4/permission-access/session")
    fun getSession(@Body sessionRequest: DMESessionRequest): Call<DMESession>

    @GET("/v1.6/permission-access/query/{sessionKey}")
    fun getFileList(@Path("sessionKey") sessionKey: String): Call<DMEFileList>

    @GET("/v1.4/permission-access/query/{sessionKey}/{fileName}")
    fun getFile(
        @Path("sessionKey") sessionKey: String,
        @Path("fileName") fileName: String
    ): Call<DMEFile>

    @Headers("Accept: application/octet-stream")
    @GET("/v1.6/permission-access/query/{sessionKey}/{fileName}")
    fun getFileBytes(
        @Path("sessionKey") sessionKey: String,
        @Path("fileName") fileName: String
    ): Single<Response<ResponseBody>>

    @Multipart
    @Headers("Accept: application/json", "cache-control: no-cache")
    @POST("/v1.6/permission-access/postbox/{id}")
    fun pushData(
        @Header("sessionKey") sessionKey: String,
        @Header("symmetricalKey") symmetricalKey: String,
        @Header("iv") iv: String,
        @Header("metadata") metadata: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("file") description: RequestBody
    ): Call<Unit>

    @Multipart
    @Headers("Accept: application/json", "cache-control: no-cache")
    @POST("/v1.6/permission-access/postbox/{id}")
    fun pushOngoingData(
        @Header("Authorization") jwt: String,
        @Header("sessionKey") sessionKey: String,
        @Header("symmetricalKey") symmetricalKey: String,
        @Header("iv") iv: String,
        @Header("metadata") metadata: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("file") description: RequestBody
    ): Single<SaasOngoingPushResponse>

    @POST("v1.6/oauth/token")
    fun refreshCredentials(@Header("Authorization") jwt: String): Call<ExchangeTokenJWT>

    @POST("v1.6/oauth/authorize")
    fun getPreauthorizationCode1(@Header("Authorization") jwt: String): Single<DMEPreAuthorizationResponse>

    @POST("v1.6/oauth/authorize")
    fun getPreauthorizationCode(@Header("Authorization") jwt: String): Call<DMEPreauthorizationResponseJWT>

    @POST("v1.6/oauth/token")
    fun exchangeAuthToken(@Header("Authorization") jwt: String): Call<DMEAuthCodeExchangeResponseJWT>

    @POST("v1.6/oauth/token")
    fun exchangeAuthToken1(@Header("Authorization") jwt: String): Call<ExchangeTokenJWT>

    @POST("v1.6/permission-access/trigger?schemaVersion=5.0.0&prefetch=false")
    fun triggerDataQuery(@Header("Authorization") jwt: String): Call<DataQueryResponse>

    /**
     * Suspend calls
     */
    @POST("v1.6/oauth/authorize")
    suspend fun fetchPreAuthorizationCode(@Header("Authorization") jwt: String): DMEPreAuthorizationResponse

    @POST("v1.6/oauth/authorize")
    fun fetchPreAuthorizationCode1(@Header("Authorization") jwt: String): Call<DMEPreAuthResponse?>

    @GET("/v1.6/permission-access/query/{sessionKey}")
    suspend fun getFileListForServices(@Path("sessionKey") sessionKey: String): DMEFileList

    // TODO: Here for testing purposes?
    @GET("v1.5/discovery/services")
    suspend fun getServicesForContract(@Header("contractId") contractId: String): ServicesResponse

    @GET("v1.5/discovery/services")
    fun getServicesForContract1(@Header("contractId") contractId: String): Single<ServicesResponse>
}