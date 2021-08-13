package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session

data class DMEPreAuthResponse(
    val session: Session,
    val token: String
)