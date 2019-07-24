package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEAccount (

    @SerializedName("identifier")
    val identifier: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("number")
    val number: String,

    @SerializedName("service")
    val service: DMEServiceDescriptor,

    @SerializedName("json")
    val json: Map<String, Any>

)