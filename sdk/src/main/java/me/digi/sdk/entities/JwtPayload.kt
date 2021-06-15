package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class Payload (
    @SerializedName("preauthorization_code")
    val preAuthorizationCode: String? = null
)