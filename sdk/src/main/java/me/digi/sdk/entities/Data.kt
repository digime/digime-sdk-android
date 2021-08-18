package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

data class Data (
    val key: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)

data class OngoingWriteData(
    val postboxId: String? = null,
    val publicKey: String? = null
)

data class OngoingData(
    val session: Session? = null,
    val data: OngoingWriteData? = null,
    val credentials: CredentialsPayload? = null
)