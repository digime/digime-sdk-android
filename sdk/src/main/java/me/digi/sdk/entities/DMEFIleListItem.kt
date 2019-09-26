package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName
import java.util.Date

internal data class DMEFileListItem (

    @SerializedName("name")
    val fileId: String,

    val updatedDate: Date
)