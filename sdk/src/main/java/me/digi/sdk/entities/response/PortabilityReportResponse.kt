package me.digi.sdk.entities.response

import me.digi.sdk.entities.payload.CredentialsPayload
import java.io.File

data class PortabilityReportResponse (
    val content: ByteArray? = null,
    val credentials: CredentialsPayload? = null
)