package me.digi.sdk.entities

import com.google.gson.Gson
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

    fun <T> fileContentAsJSON(type: Class<T>) = Gson().fromJson(String(content), type)

}