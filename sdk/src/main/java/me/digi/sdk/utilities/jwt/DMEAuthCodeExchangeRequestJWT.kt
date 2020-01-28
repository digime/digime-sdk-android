package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.util.Date

@Suppress("UNUSED")
internal class DMEAuthCodeExchangeRequestJWT (

    appId: String,
    contractId: String,
    @JwtClaim val code: String,
    @JwtClaim val codeVerifier: String

): JsonWebToken() {

    @JwtClaim val clientId = "${appId}_${contractId}"
    @JwtClaim val grantType = "authorization_code"
    @JwtClaim val redirectUri = "digime-ca-$appId"
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