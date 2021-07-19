package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class TokenReferencePayload(
    @SerializedName("expires_on")
    val expiresOn: Int? = 0,
    @SerializedName("reference_code")
    val referenceCode: String? = "",
    @SerializedName("token_type")
    val tokenType: String? = ""
)