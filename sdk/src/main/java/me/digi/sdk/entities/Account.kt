package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class Account (

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("service")
    val service: ServiceDescriptor
)