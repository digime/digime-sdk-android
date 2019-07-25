package me.digi.sdk.api.services

import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import retrofit2.http.*

internal interface DMEArgonService {

    @POST("v1/permission-access/session")
    suspend fun getSession(@Body sessionRequest: DMESessionRequest): DMESession

    @GET("/v1/permission-access/query/{sessionKey}")
    suspend fun getFileList(@Path("sessionKey") sessionKey: String): List<String>

    @GET("/v1/permission-access/query/{sessionKey}/{fileName}")
    suspend fun getFile(@Path("sessionKey") sessionKey: String, @Path("fileName") fileName: String): DMEFile

    @GET("/v1/permission-access/query/{sessionKey}/accounts.json")
    suspend fun getAccounts(@Path("sessionKey") sessionKey: String): List<DMEAccount>

}