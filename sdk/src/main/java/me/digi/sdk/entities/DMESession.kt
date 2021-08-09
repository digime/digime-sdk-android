package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName
import java.lang.ref.WeakReference
import java.util.*

data class DMESession(

    val key: String,

    @SerializedName("sessionExchangeToken")
    val exchangeToken: String,

    @SerializedName("expiry")
    val expiryDate: Date,

    var preauthorizationCode: String?,

    var authorizationCode: String?

) {

    var createdDate: Date? = null
    var scope: DataRequest? = null
    private var sessionManager: WeakReference<String>? = null
    var metadata: MutableMap<String, Any> = emptyMap<String, Any>().toMutableMap()
}