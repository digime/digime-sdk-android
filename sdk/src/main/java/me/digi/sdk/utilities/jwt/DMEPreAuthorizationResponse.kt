package me.digi.sdk.utilities.jwt

import me.digi.sdk.entities.DMESession

data class DMEPreAuthorizationResponse (
    val token: String,
    val session: DMESession
)