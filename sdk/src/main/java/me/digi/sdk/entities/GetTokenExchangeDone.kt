package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

data class TokenExchangeResponse(
    val consentData: GetConsentDone = GetConsentDone(),
    val credentials: CredentialsPayload = CredentialsPayload()
)