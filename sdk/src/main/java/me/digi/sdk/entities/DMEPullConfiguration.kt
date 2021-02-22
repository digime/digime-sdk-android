package me.digi.sdk.entities

class DMEPullConfiguration(appId: String, contractId: String, privateKeyHex: String) :
    DMEClientConfiguration(appId, contractId, privateKeyHex) {

    var guestEnabled = true
    var pollInterval = 3
    var maxStalePolls = 100
    var autoRecoverExpiredCredentials = true
}