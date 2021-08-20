package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

// TODO: Rename
data class Data(
    val key: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)

// TODO: Rename
data class WriteDataPayload(
    val postboxId: String? = null,
    val publicKey: String? = null
)

// TODO: Rename
data class OngoingData(
    val session: Session? = null,
    val data: WriteDataPayload? = null,
    val credentials: CredentialsPayload? = null
)