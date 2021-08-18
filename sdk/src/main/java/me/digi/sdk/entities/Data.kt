package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

data class Data (
    val key: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)
// TODO: Rename
data class OngoingPostboxData(
    val postboxId: String? = null,
    val publicKey: String? = null
)

// TODO: Rename
data class OngoingPostbox(
    val session: Session? = null,
    val data: OngoingPostboxData? = null,
    val credentials: CredentialsPayload? = null
)