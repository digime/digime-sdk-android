package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEFileMetadata (

    @SerializedName("mimetype")
    val mimeType: DMEMimeType,

    @SerializedName("reference")
    val reference: List<String>,

    @SerializedName("tags")
    val tags: List<String>,

    @SerializedName("contractid")
    val contractId: String

)