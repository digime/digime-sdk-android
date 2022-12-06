package me.digi.sdk.entities

data class WriteDataPayload(
    val metadata: WriteMetadata,
    val content: ByteArray,
)

data class WriteMetadata(
    val accounts: List<WriteAccount>?,
    val reference: List<String>?,
    val tags: List<String>?,
    val mimeType: String,
)

data class WriteAccount(
    val accountId: String?
)