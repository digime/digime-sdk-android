package me.digi.sdk.entities

import me.digi.sdk.entities.response.ConsentAuthResponse
import me.digi.sdk.entities.response.SaasCallbackResponse

data class GetConsentDone(
    val session: Session = Session(),
    val consentResponse: SaasCallbackResponse = SaasCallbackResponse()
)