package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName
import java.lang.ref.WeakReference
import java.util.*

data class SessionResponse(
    @SerializedName("sessionKey")
    val key: String = "",
    val expiry: Long = 0,
    val sessionExchangeToken: String = ""
) {
    var createdDate: Date? = null
    var scope: DMEDataRequest? = null
    private var sessionManager: WeakReference<String>? = null
    var metadata: MutableMap<String, Any> = emptyMap<String, Any>().toMutableMap()
}