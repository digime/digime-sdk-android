package me.digi.sdk.api.services

import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.request.AuthorizationScopeRequest
import me.digi.sdk.entities.request.DMESessionRequest
import me.digi.sdk.entities.request.Pull
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface DMEArgonService {

    @POST("v1.6/permission-access/session")
    fun getSession(@Body sessionRequest: DMESessionRequest): Call<SessionResponse>

    @GET("/v1.6/permission-access/query/{sessionKey}")
    fun getFileList(@Path("sessionKey") sessionKey: String): Call<DMEFileList>

    // TODO: Probably needs to be removed
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

    // TODO: Remove
    @Multipart
    @Headers("Accept: application/json", "cache-control: no-cache")
    @POST("/v1.4/permission-access/postbox/{id}")
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
    ): Single<OngoingWriteResponse>

    @POST("v1.6/oauth/token")
    fun refreshCredentials(@Header("Authorization") jwt: String): Call<TokenResponse>

    @POST("v1.6/oauth/token")
    fun exchangeAuthToken(@Header("Authorization") jwt: String): Call<TokenResponse>

    @POST("v1.6/permission-access/trigger?schemaVersion=5.0.0&prefetch=false")
    fun triggerDataQuery(
        @Header("Authorization") jwt: String,
        @Body scopeRequest: Pull
    ): Call<DataQueryResponse>

    @POST("v1.6/oauth/authorize")
    fun getPreAuthorizationCode(
        @Header("Authorization") jwt: String,
        @Body scopeRequest: AuthorizationScopeRequest
    ): Call<PreAuthorizationResponse>

    @GET("v1.5/discovery/services")
    fun getServicesForContract(@Header("contractId") contractId: String): Single<ServicesResponse>

    @POST("v1.6/oauth/token/reference")
    fun getReferenceCode(@Header("Authorization") jwt: String): Call<TokenReferenceResponse>

    @DELETE("v1.6/user")
    fun deleteUser(@Header("Authorization") jwt: String): Call<Unit>
}