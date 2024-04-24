package me.digi.sdk.entities

import android.net.Credentials
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.OnboardAuthResponse

data class GetOnboardDone(
    val session: Session? = null,
    val onboardResponse: OnboardAuthResponse = OnboardAuthResponse(),
    val credentials: CredentialsPayload = CredentialsPayload()
)
