package me.digi.sdk.entities

data class DMEOngoingPostbox(
    val session: DMESession? = null,
    val postbox: DMEPostbox? = null,
    val authToken: DMEOAuthToken? = null
)

data class DMESaasOngoingPostbox(
    val session: Session? = null,
    val postboxData: DMEOngoingPostboxData? = null,
    val authToken: DMETokenExchange? = null
)