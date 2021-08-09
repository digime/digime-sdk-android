package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class FileMetadata (

    @SerializedName("mimetype")
    val mimeType: MimeType,

    @SerializedName("reference")
    val reference: List<String>,

    @SerializedName("tags")
    val tags: List<String>,

    @SerializedName("contractid")
    val contractId: String

)