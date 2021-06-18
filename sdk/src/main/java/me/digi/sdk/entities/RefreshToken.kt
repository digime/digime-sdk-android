package me.digi.sdk.entities


import com.google.gson.annotations.SerializedName

data class RefreshToken(
    @SerializedName("expires_on")
    val expiresOn: Int = 0,
    val value: String = ""
)