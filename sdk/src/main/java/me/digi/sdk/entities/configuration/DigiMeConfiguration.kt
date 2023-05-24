package me.digi.sdk.entities.configuration

import me.digi.sdk.entities.payload.TokensPayload

class DigiMeConfiguration(
    appId: String,
    contractId: String,
    privateKeyHex: String,
    baseUrl: String? = "https://api.digi.me/",
    tokenPayload: TokensPayload
) :
    ClientConfiguration(appId, contractId, privateKeyHex, baseUrl, tokenPayload) {

    var pollInterval = 3
    var maxStalePolls = 200
}