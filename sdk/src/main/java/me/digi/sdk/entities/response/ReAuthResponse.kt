package me.digi.sdk.entities.response

data class ReAuthResponse(
    val success: Boolean? = null,
    val code: String? = null,
    val state: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)