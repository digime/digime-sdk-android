package me.digi.sdk.utilities.jwt

import android.util.Base64
import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import java.security.MessageDigest
import java.util.*

@Suppress("UNUSED")
internal class PreAuthorizationRequestJWT(
    appId: String,
    contractId: String,
    private val codeVerifier: String,
    credential: String? = null
) : JsonWebToken() {

    @JwtClaim
    val clientId = "${appId}_${contractId}"

    @JwtClaim
    val nonce: String

    @JwtClaim
    val state: String

    @JwtClaim
    val codeChallenge: String

    @JwtClaim
    val codeChallengeMethod = "S256"

    @JwtClaim
    val redirectUri = "digime-ca://callback-${appId}"

    @JwtClaim
    val responseMode = "query"

    @JwtClaim
    val responseType = "code"

    @JwtClaim
    val timestamp = Date().time

    @JwtClaim
    var accessToken: String? = null

    init {
        val nonceBytes = CryptoUtilities.generateSecureRandom(16)
        nonce = ByteTransformer.hexStringFromBytes(nonceBytes)

        val stateBytes = CryptoUtilities.generateSecureRandom(32)
        state = ByteTransformer.hexStringFromBytes(stateBytes)

        credential?.let { accessToken = it }

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