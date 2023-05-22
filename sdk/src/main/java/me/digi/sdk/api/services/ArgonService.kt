package me.digi.sdk.api.services

import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.request.AccountIdRequest
import me.digi.sdk.entities.request.AuthorizationScopeRequest
import me.digi.sdk.entities.request.SessionRequest
import me.digi.sdk.entities.request.Pull
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface ArgonService {

    @POST("v1.7/permission-access/session")
    fun getSession(@Body sessionRequest: SessionRequest): Call<SessionResponse>

    @GET("/v1.7/permission-access/query/{sessionKey}")
    fun getFileList(
        @Header("Authorization") jwt: String,
        @Path("sessionKey") sessionKey: String
    ): Single<FileList>

    @Headers("Accept: application/octet-stream")
    @GET("/v1.7/permission-access/query/{sessionKey}/{fileName}")
    fun getFileBytes(
        @Header("Authorization") jwt: String,
        @Path("sessionKey") sessionKey: String,
        @Path("fileName") fileName: String
    ): Single<Response<ResponseBody>>

    @Multipart
    @Headers("Accept: application/json", "cache-control: no-cache")
    @POST("/v1.7/permission-access/postbox/{id}")
    fun pushOngoingData(
        @Header("Authorization") jwt: String,
        @Header("sessionKey") sessionKey: String,
        @Header("symmetricalKey") symmetricalKey: String,
        @Header("iv") iv: String,
        @Header("metadata") metadata: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("file") description: RequestBody
    ): Single<DataWriteResponse>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/octet-stream",
        "cache-control: no-cache"
    )
    @POST("/v1.7/permission-access/import")
    fun directImport(
        @Header("Authorization") jwt: String,
        @Header("FileDescriptor") fileDescriptor: String,
        @Body file: RequestBody
    ): Single<Unit>

    @POST("v1.7/oauth/token")
    fun refreshCredentials(@Header("Authorization") jwt: String): Call<TokenResponse>

    @POST("v1.7/oauth/token")
    fun exchangeAuthToken(@Header("Authorization") jwt: String): Call<TokenResponse>

    @POST("v1.7/permission-access/trigger?schemaVersion=5.0.0&prefetch=false")
    fun triggerDataQuery(
        @Header("Authorization") jwt: String,
        @Body scopeRequest: Pull
    ): Call<DataQueryResponse>

    @POST("v1.7/oauth/authorize")
    fun getPreAuthorizationCode(
        @Header("Authorization") jwt: String,
        @Body scopeRequest: AuthorizationScopeRequest
    ): Call<PreAuthorizationResponse>

    @GET("v1.7/discovery/services")
    fun getServicesForContract(@Header("contractId") contractId: String?): Single<ServicesResponse>

    @POST("v1.7/oauth/token/reference")
    fun getReferenceCode(@Header("Authorization") jwt: String): Call<TokenReferenceResponse>

    @POST("v1.7/reference")
    fun getAccountIdReference(
        @Header("Authorization") jwt: String,
        @Body accountIdRequest: AccountIdRequest
    ): Call<AccountReferenceResponse>

    @DELETE("v1.7/user")
    fun deleteUser(@Header("Authorization") jwt: String): Call<Unit>

    @GET("v1.7/export/{serviceType}/report?")
    fun getPortabilityReport(
        @Header("Authorization") jwt: String,
        @Path("serviceType") serviceType: String,
        @Query("format") format: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Single<Response<ResponseBody>>
}