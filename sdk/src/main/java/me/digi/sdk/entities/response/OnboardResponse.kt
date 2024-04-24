package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.TokensPayload

data class OnboardResponse(
    val session: Session? = null,
    val onboardResponse: SaasCallbackResponse? = null,
    val credentials: TokensPayload? = null
)