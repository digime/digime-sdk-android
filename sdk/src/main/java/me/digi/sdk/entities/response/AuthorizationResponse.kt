package me.digi.sdk.entities.response

import me.digi.sdk.entities.Credentials
import me.digi.sdk.entities.OngoingPostboxData

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxData: OngoingPostboxData? = OngoingPostboxData(),
    val credentials: Credentials? = Credentials()
)