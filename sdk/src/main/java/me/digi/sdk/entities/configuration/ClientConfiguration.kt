package me.digi.sdk.entities.configuration

import me.digi.sdk.utilities.DMELog

abstract class ClientConfiguration(
    var appId: String,
    var contractId: String,
    var privateKeyHex: String,
    var baseUrl: String?
) {

    var globalTimeout: Int = 62
    var maxConcurrentRequests: Int = 5

    var retryOnFail: Boolean = false
    var retryDelay: Int = 750
    var retryWithExponentialBackOff: Boolean = true
    var maxRetryCount: Int = 5

    var autoRecoverExpiredCredentials = true

    var debugLogEnabled: Boolean = false
        set(value) {
            field = value
            DMELog.debugLogEnabled = value
        }
}