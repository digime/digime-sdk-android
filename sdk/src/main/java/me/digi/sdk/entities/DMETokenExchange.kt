package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMETokenExchange(
    @SerializedName("access_token")
    val accessToken: AccessToken = AccessToken(),
    val consentid: String = "",
    val identifier: Identifier = Identifier(),
    @SerializedName("refresh_token")
    val refreshToken: RefreshToken = RefreshToken(),
    @SerializedName("token_type")
    val tokenType: String = ""
)