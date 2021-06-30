package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEPushPayload(

    @JvmField
    val dmePostbox: DMEPostbox,

    @JvmField
    val metadata: ByteArray,

    @JvmField
    val content: ByteArray,

    @SerializedName("mimetype")
    val mimeType: DMEMimeType
)

data class SaasPushPayload(
    val dmePostbox: SaasDMEPostobx,
    val metadata: ByteArray,
    val content: ByteArray,
    @SerializedName("mimetype")
    val mimeType: DMEMimeType
)

data class SaasDMEPostobx(
    val key: String? = null,
    @SerializedName("postboxid")
    val postboxId: String? = null,
    @SerializedName("publickey")
    val publicKey: String? = null,
)