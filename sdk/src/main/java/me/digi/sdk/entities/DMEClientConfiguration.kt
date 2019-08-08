package me.digi.sdk.entities

abstract class DMEClientConfiguration (

    var appId: String,
    var contractId: String

) {

    var globalTimeout: Int = 25
    var retryOnFail: Boolean = true
    var retryDelay: Int = 750
    var retryWithExponentialBackOff: Boolean = true
    var maxRetryCount: Int = 5
    var maxConcurrentRequests: Int = 5
    var debugLogEnabled: Boolean = false
    var baseUrl: String = "https://api.digi.me/"

}