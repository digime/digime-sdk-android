package me.digi.sdk.entities

import me.digi.sdk.entities.payload.TokensPayload
import me.digi.sdk.entities.response.OnboardAuthResponse

data class GetOnboardDone(
    val session: Session? = null,
    val onboardResponse: OnboardAuthResponse = OnboardAuthResponse(),
    val credentials: TokensPayload = TokensPayload()
)
