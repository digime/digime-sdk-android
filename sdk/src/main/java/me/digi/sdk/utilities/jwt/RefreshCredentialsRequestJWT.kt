package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.util.*

internal class RefreshCredentialsRequestJWT(

    appId: String,
    contractId: String,
    @JwtClaim val refreshToken: String

) : JsonWebToken() {

    @JwtClaim
    val clientId = "${appId}_${contractId}"

    @JwtClaim
    val timestamp = Date().time

    @JwtClaim
    val grantType = "refresh_token"

    @JwtClaim
    val redirectUri = "digime-ca://callback"

    @JwtClaim
    val nonce: String

    init {
        val nonceBytes = DMECryptoUtilities.generateSecureRandom(16)
        nonce = DMEByteTransformer.hexStringFromBytes(nonceBytes)
    }

    override fun tokenize(): String {
        return "Bearer " + super.tokenize()
    }
}