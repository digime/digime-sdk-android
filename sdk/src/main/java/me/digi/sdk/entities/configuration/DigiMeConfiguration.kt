package me.digi.sdk.entities.configuration

class DigiMeConfiguration(
    appId: String,
    contractId: String,
    privateKeyHex: String,
    baseUrl: String? = "https://api.digi.me/"
) :
    ClientConfiguration(appId, contractId, privateKeyHex, baseUrl) {

    var pollInterval = 3
    var maxStalePolls = 200
}