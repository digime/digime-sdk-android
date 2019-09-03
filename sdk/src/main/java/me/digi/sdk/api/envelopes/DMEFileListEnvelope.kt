package me.digi.sdk.api.envelopes

import com.google.gson.annotations.SerializedName

internal data class DMEFileListEnvelope (

    @SerializedName("fileList")
    val fileIds: List<String>
)