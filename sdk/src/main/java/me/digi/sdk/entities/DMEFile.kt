package me.digi.sdk.entities

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class DMEFile (

    @JvmField
    val metadata: DMEFileMetadata?,

    @SerializedName("mimetype")
    val mimeType: DMEMimeType,

    @JvmField
    val content: ByteArray

) {

    lateinit var identifier: String

    internal fun <T> fileContentAs(type: Class<T>) = Gson().fromJson(String(content), type)
    fun fileContentAsJSON(): JsonElement? = Gson().toJsonTree(String(content))

}