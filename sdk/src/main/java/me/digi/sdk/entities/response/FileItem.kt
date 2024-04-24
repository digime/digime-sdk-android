package me.digi.sdk.entities.response

import com.google.gson.Gson
import com.google.gson.JsonElement
import me.digi.sdk.entities.payload.CredentialsPayload

data class FileItem(
    val fileContent: String? = "",
    val fileName: String? = "",
    var credentials: CredentialsPayload? = null
) {
    lateinit var identifier: String

    internal fun <T> fileContentAs(type: Class<T>) = Gson().fromJson(fileContent, type)
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(fileContent)
}