package me.digi.sdk.entities.response

import me.digi.sdk.entities.payload.TokensPayload

data class WriteDataResponse (
    val dataWritten: Boolean? = null,
    val credentials: TokensPayload? = null
)