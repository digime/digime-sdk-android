package me.digi.sdk.entities.payload

import com.google.gson.annotations.SerializedName

data class PreAuthorizationCodePayload (
    @SerializedName("preauthorization_code")
    val preAuthorizationCode: String? = null
)