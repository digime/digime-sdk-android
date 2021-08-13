package me.digi.sdk.entities

data class Metadata(
    val objectCount: Int? = 0,
    val objectType: String? = "",
    val objectVersion: String? = "",
    val serviceGroup: String? = "",
    val serviceName: String? = ""
)

data class HeaderMetadataPayload(
    val compression: String = "",
    val metadata: Metadata? = Metadata()
)