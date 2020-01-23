package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.util.*

@Suppress("UNUSED")
internal class TriggerDataQueryRequestJWT (

    appId: String,
    contractId: String,
    @JwtClaim val sessionKey: String,
    @JwtClaim val accessToken: String

): JsonWebToken() {

    @JwtClaim val redirectUri = "digime-ca-$appId"
    @JwtClaim val clientId = "${appId}_${contractId}"
    @JwtClaim val timestamp = Date().time
    @JwtClaim val nonce: String

    init {
        val nonceBytes = DMECryptoUtilities.generateSecureRandom(16)
        nonce = DMEByteTransformer.hexStringFromBytes(nonceBytes)
    }

    override fun tokenize(): String {
        return "Bearer " + super.tokenize()
    }
}