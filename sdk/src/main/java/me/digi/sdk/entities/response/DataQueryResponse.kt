package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.TokensPayload

data class DataQueryResponse(val session: Session, var credentials: TokensPayload)