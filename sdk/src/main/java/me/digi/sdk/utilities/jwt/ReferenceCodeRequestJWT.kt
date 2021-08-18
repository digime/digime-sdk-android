package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.util.*

@Suppress("UNUSED")
internal class ReferenceCodeRequestJWT(
    appId: String,
    contractId: String,
    @JwtClaim val accessToken: String
) : JsonWebToken() {

    @JwtClaim
    val redirectUri = "digime-ca://callback"

    @JwtClaim
    val clientId = "${appId}_${contractId}"

    @JwtClaim
    val timestamp = Date().time

    @JwtClaim
    val nonce: String

    init {
        val nonceBytes = DMECryptoUtilities.generateSecureRandom(16)
        nonce = DMEByteTransformer.hexStringFromBytes(nonceBytes)
    }

    override fun tokenize(): String = "Bearer " + super.tokenize()
}