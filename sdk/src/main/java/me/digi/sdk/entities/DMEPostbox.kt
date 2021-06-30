package me.digi.sdk.entities

data class DMEPostbox (
    val key: String? = null,
    val postboxId: String? = null,
    val publicKey: String? = null
)

data class DMEOngoingPostboxData(
    val postboxId: String? = null,
    val publicKey: String? = null
)