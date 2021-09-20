package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

data class GetTokenExchangeDone(
    val consentData: GetConsentDone = GetConsentDone(),
    val credentials: CredentialsPayload = CredentialsPayload()
)