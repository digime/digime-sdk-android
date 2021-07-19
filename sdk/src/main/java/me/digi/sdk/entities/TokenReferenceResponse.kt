package me.digi.sdk.entities

data class TokenReferenceResponse(
    val session: Session = Session(),
    val token: String = ""
)