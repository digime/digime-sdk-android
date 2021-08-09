package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session

data class TokenReferenceResponse(
    val session: Session = Session(),
    val token: String = ""
)