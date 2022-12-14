package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import java.util.*

@Suppress("UNUSED")
internal class AuthCodeExchangeRequestJWT(

    appId: String,
    contractId: String,
    @JwtClaim val code: String,
    @JwtClaim val codeVerifier: String

) : JsonWebToken() {

    @JwtClaim
    val clientId = "${appId}_${contractId}"

    @JwtClaim
    val grantType = "authorization_code"

    @JwtClaim
    val redirectUri = "digime-ca://callback-${appId}"

    @JwtClaim
    val timestamp = Date().time

    @JwtClaim
    val nonce: String

    init {
        val nonceBytes = CryptoUtilities.generateSecureRandom(16)
        nonce = ByteTransformer.hexStringFromBytes(nonceBytes)
    }

    override fun tokenize(): String {
        return "Bearer " + super.tokenize()
    }
}