package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName
import java.lang.ref.WeakReference
import java.util.Date

data class DMESession (

    @SerializedName("sessionKey")
    val key: String,

    @SerializedName("sessionExchangeToken")
    val exchangeToken: String,

    @SerializedName("expiry")
    val expiryDate: Date,

    val accessToken: String?,

    var refreshToken: String?,

    var authorizationCode: String?

) {

    var createdDate: Date? = null
    var scope: DMEDataRequest? = null
    private var sessionManager: WeakReference<String>? = null
    var metadata: Map<String, Any> = emptyMap()

}