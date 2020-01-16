package me.digi.sdk.api.helpers

import android.util.Base64
import me.digi.sdk.entities.api.DMEJsonWebToken
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

internal class DMEAuthCodeRedemptionHelper {

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    private val encFlags = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun buildForPreAuthRequest(contractId: String, appId: String): DMEJsonWebToken {

        codeVerifier = generateCodeVerifier()
        codeChallenge = generateCodeChallenge(codeVerifier)

        val header = DMEJsonWebToken.Header("PS512", "JWT", null, null)
        val paylod = DMEJsonWebToken.Payload.PreAuthRequest("${appId}_${contractId}", codeChallenge, "S256",
            Base64.encodeToString(DMECryptoUtilities.generateSecureRandom(32), encFlags), "digime-ca-${appId}", "query",
            "code", generateNonce(), Date().time.toDouble())
        val jwt = DMEJsonWebToken(header, paylod)
        return jwt
    }

    private fun generateCodeVerifier(): String {
        val bytes = DMECryptoUtilities.generateSecureRandom(64)
        return DMEByteTransformer.hexStringFromBytes(bytes)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val dgst = MessageDigest.getInstance("SHA-256")
        dgst.update(verifier.toByteArray())
        val raw = dgst.digest()
        return Base64.encodeToString(raw, encFlags)
    }

    private fun generateNonce(): String {
        val bytes = DMECryptoUtilities.generateSecureRandom(16)
        return DMEByteTransformer.hexStringFromBytes(bytes)
    }
}