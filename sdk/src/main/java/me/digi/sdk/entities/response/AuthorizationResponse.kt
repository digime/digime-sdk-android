package me.digi.sdk.entities.response

import me.digi.sdk.entities.EssentialCredentials
import me.digi.sdk.entities.OngoingPostboxData

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxData: OngoingPostboxData? = OngoingPostboxData(),
    val credentials: EssentialCredentials? = EssentialCredentials()
)