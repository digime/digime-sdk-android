package me.digi.sdk.entities

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class DMEFile (

    @JvmField
    val metadata: DMEFileMetadata?,

    @SerializedName("mimetype")
    val mimeType: DMEMimeType,

    @JvmField
    val content: ByteArray

) {

    lateinit var identifier: String

    fun fileContentAsJSON() = Gson().toJsonTree(content)

}