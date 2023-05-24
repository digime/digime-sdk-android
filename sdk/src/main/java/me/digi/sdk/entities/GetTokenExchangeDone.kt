package me.digi.sdk.entities

import me.digi.sdk.entities.payload.TokenPayload

data class TokenExchangeResponse(
    val consentData: GetConsentDone = GetConsentDone()
)