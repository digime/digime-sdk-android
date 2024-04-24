package me.digi.sdk.entities.response

import com.google.gson.Gson
import com.google.gson.JsonElement
import me.digi.sdk.entities.payload.TokensPayload

data class FileItem(
    val fileContent: String? = "",
    val fileName: String? = "",
    var credentials: TokensPayload? = null
) {
    lateinit var identifier: String

    internal fun <T> fileContentAs(type: Class<T>) = Gson().fromJson(fileContent, type)
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(fileContent)
}