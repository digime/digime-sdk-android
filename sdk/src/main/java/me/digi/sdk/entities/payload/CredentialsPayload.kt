package me.digi.sdk.entities.payload

import com.google.gson.annotations.SerializedName

data class AccessToken(
    @SerializedName("expires_on")
    val expiresOn: Int = 0,
    val value: String = ""
)

data class Identifier(val id: String = "")

data class RefreshToken(
    @SerializedName("expires_on")
    val expiresOn: Int = 0,
    val value: String = ""
)

data class CredentialsPayload(
    @SerializedName("access_token")
    val accessToken: AccessToken = AccessToken(),
    val consentid: String = "",
    val identifier: Identifier = Identifier(),
    @SerializedName("refresh_token")
    val refreshToken: RefreshToken = RefreshToken(),
    @SerializedName("token_type")
    val tokenType: String = ""
)