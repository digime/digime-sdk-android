package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class ServiceDescriptor(
    @SerializedName("name")
    val name: String,
    @SerializedName("logo")
    val logoUrl: String

)