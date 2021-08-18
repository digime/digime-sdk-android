package me.digi.sdk.entities.payload

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.MimeType

// TODO: Rename
data class DataPayload(
    val data: Data,
    val metadata: ByteArray,
    val content: ByteArray,
    @SerializedName("mimetype")
    val mimeType: MimeType
)