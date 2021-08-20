package me.digi.sdk.entities.response

import me.digi.sdk.entities.Session

data class DataWriteResponse(
    val expires: Int = 0,
    val session: Session = Session(),
    val status: String = ""
)