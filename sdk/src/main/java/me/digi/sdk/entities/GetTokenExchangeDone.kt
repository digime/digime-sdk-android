package me.digi.sdk.entities

import me.digi.sdk.entities.payload.TokensPayload

data class TokenExchangeResponse(
    val consentData: GetConsentDone = GetConsentDone(),
    val credentials: TokensPayload = TokensPayload()
)