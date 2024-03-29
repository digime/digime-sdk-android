package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
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
        val nonceBytes = CryptoUtilities.generateSecureRandom(16)
        nonce = ByteTransformer.hexStringFromBytes(nonceBytes)
    }

    override fun tokenize(): String = "Bearer " + super.tokenize()
}