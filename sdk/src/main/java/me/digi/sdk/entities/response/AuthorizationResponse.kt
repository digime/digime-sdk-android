package me.digi.sdk.entities.response

import me.digi.sdk.entities.EssentialCredentials

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null,
    val credentials: EssentialCredentials? = EssentialCredentials()
)