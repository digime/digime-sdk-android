package me.digi.sdk.entities.response

import me.digi.sdk.entities.Credentials
import me.digi.sdk.entities.WriteDataPayload

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxData: WriteDataPayload? = WriteDataPayload(),
    val credentials: Credentials? = Credentials()
)