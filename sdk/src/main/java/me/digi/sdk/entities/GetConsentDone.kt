package me.digi.sdk.entities

import me.digi.sdk.entities.response.ConsentAuthResponse

data class GetConsentDone(
    val session: Session = Session(),
    val consentResponse: ConsentAuthResponse = ConsentAuthResponse()
)