package me.digi.sdk.entities.payload

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.Postbox

data class DMEPushPayload(
    val postbox: Postbox,
    val metadata: ByteArray,
    val content: ByteArray,
    @SerializedName("mimetype")
    val mimeType: MimeType
)