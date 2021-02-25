package me.digi.sdk.entities

class DMEPushConfiguration(appId: String, contractId: String, privateKeyHex: String) :
    DMEClientConfiguration(appId, contractId, privateKeyHex) {
    var autoRecoverExpiredCredentials = true
}