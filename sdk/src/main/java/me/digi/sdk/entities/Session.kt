package me.digi.sdk.entities

data class Session(
    val expiry: Long = 0,
    val key: String = ""
) {
    var metadata: MutableMap<String, Any> = emptyMap<String, Any>().toMutableMap()
}