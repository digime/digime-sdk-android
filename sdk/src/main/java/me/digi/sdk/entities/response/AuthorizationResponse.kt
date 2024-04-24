package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session

data class AuthorizationResponse(
    val session: Session? = null,
    val authResponse: SaasCallbackResponse? = null
)