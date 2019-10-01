package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEPostboxFile (

    @JvmField
    val dmePostbox: DMEPostbox,

    @JvmField
    val metadata: ByteArray,

    @JvmField
    val content: ByteArray,

    @SerializedName("mimetype")
    val mimeType: DMEMimeType
)