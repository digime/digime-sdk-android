package me.digi.sdk.entities

class DMEPullClientConfiguration (

    appId: String,
    contractId: String,
    var privateKeyHex: String

): DMEClientConfiguration(appId, contractId) {

    var guestEnabled = true

}