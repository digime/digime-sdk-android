package me.digi.sdk.utilities.jwt

import me.digi.sdk.entities.DMESession

data class DMEAUthResponse (
    val token: String,
    val session: DMESession
)