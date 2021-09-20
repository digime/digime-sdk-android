package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session

data class PreAuthorizationResponse(
    val session: Session,
    val token: String
)