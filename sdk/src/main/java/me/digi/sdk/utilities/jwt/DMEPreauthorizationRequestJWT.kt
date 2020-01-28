package me.digi.sdk.utilities.jwt

import android.util.Base64
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.security.MessageDigest
import java.util.*

@Suppress("UNUSED")
internal class DMEPreauthorizationRequestJWT(appId: String, contractId: String, val codeVerifier: String): JsonWebToken() {

    @JwtClaim val clientId = "${appId}_${contractId}"
    @JwtClaim val nonce: String
    @JwtClaim val state: String
    @JwtClaim val codeChallenge: String
    @JwtClaim val codeChallengeMethod = "S256"
    @JwtClaim val redirectUri = "digime-ca-$appId"
    @JwtClaim val responseMode = "query"
    @JwtClaim val responseType = "code"
    @JwtClaim val timestamp = Date().time

    init {
        val nonceBytes = DMECryptoUtilities.generateSecureRandom(16)
        nonce = DMEByteTransformer.hexStringFromBytes(nonceBytes)

        val stateBytes = DMECryptoUtilities.generateSecureRandom(32)
        state = DMEByteTransformer.hexStringFromBytes(stateBytes)

        codeChallenge = generateCodeChallenge()
    }

    private fun generateCodeChallenge(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(codeVerifier.toByteArray())
        val bytes = digest.digest()
        return Base64.encodeToString(bytes, BASE64_FLAGS)
    }

    override fun tokenize(): String {
        return "Bearer " + super.tokenize()
    }
}