package me.digi.sdk.entities.response

data class ConsentAuthResponse(
    val code: String? = null,
    val state: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)