package me.digi.sdk.api.services

import me.digi.sdk.api.envelopes.DMEFileListEnvelope
import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import retrofit2.Call
import retrofit2.http.*

internal interface DMEArgonService {

    @POST("v1.3/permission-access/session")
    fun getSession(@Body sessionRequest: DMESessionRequest): Call<DMESession>

    @GET("/v1.3/permission-access/query/{sessionKey}")
    fun getFileList(@Path("sessionKey") sessionKey: String): Call<DMEFileListEnvelope>

    @GET("/v1.3/permission-access/query/{sessionKey}/{fileName}")
    fun getFile(@Path("sessionKey") sessionKey: String, @Path("fileName") fileName: String): Call<DMEFile>
}