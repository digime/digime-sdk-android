package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload

data class AuthorizationResponse(
    val session: Session? = null,
    val authResponse: ConsentAuthResponse? = null,
    val credentials: CredentialsPayload? = null
)