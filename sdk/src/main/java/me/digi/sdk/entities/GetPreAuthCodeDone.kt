package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.PreAuthorizationCodePayload

data class GetPreAuthCodeDone(
    val session: Session = Session(),
    val payload: PreAuthorizationCodePayload = PreAuthorizationCodePayload()
)