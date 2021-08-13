package me.digi.sdk.unify

import me.digi.sdk.entities.configuration.ClientConfiguration

class DigiMeConfiguration(appId: String, contractId: String, privateKeyHex: String) :
    ClientConfiguration(appId, contractId, privateKeyHex) {

    var guestEnabled = true
    var pollInterval = 3
    var maxStalePolls = 100
}