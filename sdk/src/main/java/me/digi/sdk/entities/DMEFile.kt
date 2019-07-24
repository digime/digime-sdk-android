package me.digi.sdk.entities

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class DMEFile (

    @SerializedName("identifier")
    val identifier: String,

    @SerializedName("metadata")
    val metadata: DMEFileMetadata,

    @SerializedName("mimetype")
    val mimeType: DMEMimeType,

    @SerializedName("content")
    val content: ByteArray

) {

    fun fileContentAsJSON(): Map<String, Any> {
        val jsonString = String(content, Charsets.UTF_8)

        val gson = Gson()
        val typeToken = object: TypeToken<Map<String, Any>>() {}.type
        val contentMap = gson.fromJson<Map<String, Any>>(jsonString, typeToken)

        return contentMap
    }

}