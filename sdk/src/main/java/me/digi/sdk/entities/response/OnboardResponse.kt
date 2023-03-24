package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload

data class OnboardResponse(
    val session: Session? = null,
    val onboardResponse: OnboardAuthResponse? = null,
    val credentials: CredentialsPayload? = null
)