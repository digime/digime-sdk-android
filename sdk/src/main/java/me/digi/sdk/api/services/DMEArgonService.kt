package me.digi.sdk.api.services

import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import retrofit2.Call
import retrofit2.http.*

interface DMEArgonService {

    @POST("v1/permission-access/session")
    fun getSession(@Body sessionRequest: DMESessionRequest): Call<DMESession>

    @GET("/v1/permission-access/query/{sessionKey}")
    fun getFileList(@Path("sessionKey") sessionKey: String): Call<List<String>>

    @GET("/v1/permission-access/query/{sessionKey}/{fileName}")
    fun getFile(@Path("sessionKey") sessionKey: String, @Path("fileName") fileName: String): Call<DMEFile>

    @GET("/v1/permission-access/query/{sessionKey}/accounts.json")
    fun getAccounts(@Path("sessionKey") sessionKey: String): Call<List<DMEAccount>>

}