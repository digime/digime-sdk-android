package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEServiceDescriptor (

    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String

)