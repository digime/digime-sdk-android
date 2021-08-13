package me.digi.sdk.entities.configuration

class ReadConfiguration(appId: String, contractId: String, privateKeyHex: String) :
    ClientConfiguration(appId, contractId, privateKeyHex) {

    var guestEnabled = true
    var pollInterval = 3
    var maxStalePolls = 100
}