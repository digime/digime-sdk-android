package me.digi.sdk.entities.response

data class AuthorizeResponse(
    val postboxId: String? = null,
    val publicKey: String? = null,
    val sessionKey: String? = null,
    val accessToken: String? = null
)