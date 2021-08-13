package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

data class GetCredentialsDone(
    val session: Session = Session(),
    val credentials: CredentialsPayload = CredentialsPayload()
)