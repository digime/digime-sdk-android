package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEFileListItem (
    @SerializedName("name")
    val fileId: String = "",
    val updatedDate: Long = 0,
    val objectVersion: String = "",
)