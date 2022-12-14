package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import java.util.*

internal class UserDeletionRequestJWT(
    appId: String,
    contractId: String,
    @JwtClaim val accessToken: String
) : JsonWebToken() {

    @JwtClaim
    val clientId = "${appId}_${contractId}"

    @JwtClaim
    val nonce: String

    @JwtClaim
    val redirectUri = "digime-ca://callback-${appId}"

    @JwtClaim
    val timestamp = Date().time

    init {
        val nonceBytes = CryptoUtilities.generateSecureRandom(16)
        nonce = ByteTransformer.hexStringFromBytes(nonceBytes)
    }

    override fun tokenize(): String = "Bearer " + super.tokenize()
}