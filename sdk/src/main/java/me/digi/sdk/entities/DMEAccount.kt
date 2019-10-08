package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEAccount (

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("service")
    val service: DMEServiceDescriptor
)