package me.digi.sdk.entities.response

import me.digi.sdk.entities.payload.TokensPayload

data class DeleteUserResponse (
    val userDeleted: Boolean? = null,
    val credentials: TokensPayload? = null
)