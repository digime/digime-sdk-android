package me.digi.sdk.entities

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class DMEFile (

    @JvmField
    val metadata: DMEFileMetadata?,

    @JvmField
    @SerializedName("mimetype")
    val mimeType: DMEMimeType,

    @JvmField
    @SerializedName("content")
    val fileContent: String

) {

    lateinit var identifier: String

    internal fun <T> fileContentAs(type: Class<T>) = Gson().fromJson(fileContent, type)
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(fileContent)

}