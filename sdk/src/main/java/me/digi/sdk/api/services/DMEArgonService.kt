package me.digi.sdk.api.services

import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.saas.serviceentities.ServicesResponse
import me.digi.sdk.utilities.jwt.DMEAuthCodeExchangeResponseJWT
import me.digi.sdk.utilities.jwt.DMEPreAuthorizationResponse
import me.digi.sdk.utilities.jwt.DMEPreauthorizationResponseJWT
import me.digi.sdk.utilities.jwt.RefreshCredentialsResponseJWT
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

internal interface DMEArgonService {

    @POST("v1.4/permission-access/session")
    fun getSession(@Body sessionRequest: DMESessionRequest): Call<DMESession>

    @GET("/v1.6/permission-access/query/{sessionKey}")
    fun getFileList(@Path("sessionKey") sessionKey: String): Call<DMEFileList>

    @GET("/v1.6/permission-access/query/{sessionKey}/{fileName}")
    fun getFile(
        @Path("sessionKey") sessionKey: String,
        @Path("fileName") fileName: String
    ): Call<DMEFile>

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
    @POST("/v1.5/permission-access/postbox/{id}")
    fun pushOngoingData(
        @Header("Authorization") jwt: String,
        @Header("sessionKey") sessionKey: String,
        @Header("symmetricalKey") symmetricalKey: String,
        @Header("iv") iv: String,
        @Header("metadata") metadata: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("file") description: RequestBody
    ): Call<Unit>

    @POST("v1.6/oauth/token")
    fun refreshCredentials(@Header("Authorization") jwt: String): Call<RefreshCredentialsResponseJWT>

    @POST("v1.6/oauth/authorize")
    fun getPreauthorizationCode1(@Header("Authorization") jwt: String): Single<DMEPreAuthorizationResponse>

    @POST("v1.6/oauth/authorize")
    fun getPreauthorizationCode(@Header("Authorization") jwt: String): Call<DMEPreauthorizationResponseJWT>

    @POST("v1.6/oauth/token")
    fun exchangeAuthToken(@Header("Authorization") jwt: String): Call<DMEAuthCodeExchangeResponseJWT>

    @POST("v1.6/permission-access/trigger?schemaVersion=5.0.0&prefetch=false")
    fun triggerDataQuery(@Header("Authorization") jwt: String): Call<Unit>

    /**
     * Suspend calls
     */
    @POST("v1.6/oauth/authorize")
    suspend fun fetchPreAuthorizationCode(@Header("Authorization") jwt: String): DMEPreAuthorizationResponse

    @GET("/v1.6/permission-access/query/{sessionKey}")
    suspend fun getFileListForServices(@Path("sessionKey") sessionKey: String): DMEFileList

    // TODO: Here for testing purposes?
    @GET("v1.5/discovery/services")
    suspend fun getServicesForContract(@Header("contractId") contractId: String): ServicesResponse
}