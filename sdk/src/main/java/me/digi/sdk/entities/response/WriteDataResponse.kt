package me.digi.sdk.entities.response

import me.digi.sdk.entities.payload.CredentialsPayload

data class WriteDataResponse (
    val dataWritten: Boolean? = null,
    val credentials: CredentialsPayload? = null
)