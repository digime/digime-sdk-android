package me.digi.saas.entities

data class AuthData(
    val sessionKey: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)