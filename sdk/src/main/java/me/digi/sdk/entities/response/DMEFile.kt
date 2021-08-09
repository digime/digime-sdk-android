package me.digi.sdk.entities

import com.google.gson.Gson
import com.google.gson.JsonElement

data class Status(val state: String = "")

data class DMEFile(
    val fileContent: String? = "",
    val status: Status = Status()
) {
    lateinit var identifier: String

    internal fun <T> fileContentAs(type: Class<T>) = Gson().fromJson(fileContent, type)
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(fileContent)
}