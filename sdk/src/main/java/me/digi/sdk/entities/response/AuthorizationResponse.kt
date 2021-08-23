package me.digi.sdk.entities.response

import me.digi.sdk.entities.Credentials
import me.digi.sdk.entities.WriteDataInfo

data class AuthorizationResponse(
    val sessionKey: String? = null,
    val postboxData: WriteDataInfo? = WriteDataInfo(),
    val credentials: Credentials? = Credentials()
)