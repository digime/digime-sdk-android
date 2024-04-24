package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload

data class DataQueryResponse(val session: Session, var credentials: CredentialsPayload)