package me.digi.sdk.entities

data class SaasOngoingPushResponse(
    val expires: Int = 0,
    val session: Session = Session(),
    val status: String = ""
)