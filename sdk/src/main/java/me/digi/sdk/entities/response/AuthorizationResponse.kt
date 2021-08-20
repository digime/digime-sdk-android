package me.digi.sdk.entities.response

import me.digi.sdk.entities.Credentials
import me.digi.sdk.entities.WriteDataInfoPayload

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxData: WriteDataInfoPayload? = WriteDataInfoPayload(),
    val credentials: Credentials? = Credentials()
)