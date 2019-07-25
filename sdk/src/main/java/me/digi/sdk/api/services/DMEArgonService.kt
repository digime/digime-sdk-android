package me.digi.sdk.api.services

import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface DMEArgonService {

    @Headers (
        "Content-Type: application/json",
        "foo"
    )
    fun getFileList( @Path("sessionKey") sessionKey: String): List<String>

    fun getFile(@Path("sessionKey") sessionKey: String, @Path("fileName") fileName: String): DMEFile

    fun getAccounts(@Path("sessionKey") sessionKey: String): List<DMEAccount>

}