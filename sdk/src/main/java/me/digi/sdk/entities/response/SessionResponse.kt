package me.digi.sdk.entities.response

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.DataRequest
import java.lang.ref.WeakReference
import java.util.*

data class SessionResponse(
    @SerializedName("sessionKey")
    val key: String = "",
    val expiry: Long = 0,
    val sessionExchangeToken: String = ""
) {
    var createdDate: Date? = null
    var scope: DataRequest? = null
    private var sessionManager: WeakReference<String>? = null
    var metadata: MutableMap<String, Any> = emptyMap<String, Any>().toMutableMap()
}