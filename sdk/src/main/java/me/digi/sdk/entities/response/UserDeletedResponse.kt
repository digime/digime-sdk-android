package me.digi.sdk.entities.response

import me.digi.sdk.entities.payload.CredentialsPayload

data class DeleteUserResponse (
    val userDeleted: Boolean? = null,
    val credentials: CredentialsPayload? = null
)