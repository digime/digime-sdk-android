package me.digi.sdk.entities.response

import me.digi.sdk.entities.Credentials
import me.digi.sdk.entities.OngoingWriteData

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxData: OngoingWriteData? = OngoingWriteData(),
    val credentials: Credentials? = Credentials()
)