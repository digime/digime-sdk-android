package me.digi.sdk.entities

data class DMEOngoingPostbox(
    val session: DMESession? = null,
    val postbox: DMEPostbox? = null,
    val authToken: DMEOAuthToken? = null
)