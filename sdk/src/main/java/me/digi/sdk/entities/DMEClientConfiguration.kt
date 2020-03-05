package me.digi.sdk.entities

import me.digi.sdk.utilities.DMELog

abstract class DMEClientConfiguration (

    var appId: String,
    var contractId: String

) {

    var globalTimeout: Int = 25
    var maxConcurrentRequests: Int = 5

    var retryOnFail: Boolean = false
    var retryDelay: Int = 750
    var retryWithExponentialBackOff: Boolean = true
    var maxRetryCount: Int = 5

    var debugLogEnabled: Boolean = false
        set(value) {
            field = value
            DMELog.debugLogEnabled = value
        }

    var baseUrl: String = "https://api.digi.me/"

}