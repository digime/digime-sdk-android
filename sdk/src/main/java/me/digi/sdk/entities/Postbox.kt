package me.digi.sdk.entities

import me.digi.sdk.entities.payload.CredentialsPayload

data class Postbox (
    val key: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)

data class OngoingPostboxData(
    val postboxId: String? = null,
    val publicKey: String? = null
)

data class OngoingPostbox(
    val session: Session? = null,
    val postboxData: OngoingPostboxData? = null,
    val authToken: CredentialsPayload? = null
)