package me.digi.sdk.entities.response

import com.google.gson.Gson
import com.google.gson.JsonElement

data class FileItemBytes(
    val fileContent: ByteArray? = null
) {
    lateinit var identifier: String
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(fileContent)
}