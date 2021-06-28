package me.digi.sdk.entities

data class HeaderMetadata(
    val compression: String = "",
    val metadata: Metadata? = Metadata()
)