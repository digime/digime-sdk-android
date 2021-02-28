package me.digi.ongoingpostbox.domain

data class OngoingPostboxResponseBody(
    val sessionKey: String? = "",
    val postboxId: String? = "",
    val publicKey: String? = "",
    val digiMeVersion: String? = "",
    val accessToken: String? = "",
    val expiresOn: String? = "",
    val refreshToken: String? = "",
    val tokenType: String? = ""
)
