package me.digi.sdk.entities

import java.lang.ref.WeakReference
import java.util.Date

data class DMESession (

    val key: String,
    val exchangeToken: String,
    val expiryDate: Date,
    val createdDate: Date,
    val scope: DMEDataRequest,
    private val sessionManager: WeakReference<String>

) {

    var metadata: Map<String, Any> = emptyMap()

}