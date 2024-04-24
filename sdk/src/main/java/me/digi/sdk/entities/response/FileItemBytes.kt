package me.digi.sdk.entities.response

import com.google.gson.Gson
import com.google.gson.JsonElement
import me.digi.sdk.entities.payload.CredentialsPayload

data class FileItemBytes(
    val fileContent: ByteArray? = null,
    val credentials: CredentialsPayload? = null
) {
    lateinit var identifier: String
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(fileContent)
}