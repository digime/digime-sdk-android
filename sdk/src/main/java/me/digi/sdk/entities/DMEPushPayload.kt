package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEPushPayload(
    val dmePostbox: DMEPostbox,
    val metadata: ByteArray,
    val content: ByteArray,
    @SerializedName("mimetype")
    val mimeType: DMEMimeType
)