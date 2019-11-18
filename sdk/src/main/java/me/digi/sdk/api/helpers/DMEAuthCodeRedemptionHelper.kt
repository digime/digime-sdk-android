package me.digi.sdk.api.helpers

import android.util.Base64
import me.digi.sdk.entities.api.DMEJsonWebToken
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

internal class DMEAuthCodeRedemptionHelper {

    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    fun buildForPreAuthRequest(contractId: String): DMEJsonWebToken {

        codeVerifier = generateCodeVerifier()
        codeChallenge = generateCodeChallenge(codeVerifier)

        val header = DMEJsonWebToken.Header("PS512", "JWT", null, null)
        val paylod = DMEJsonWebToken.Payload.PreAuthRequest(contractId, codeChallenge, "S256",
            Base64.encodeToString(DMECryptoUtilities.generateSecureRandom(32), Base64.DEFAULT), "", "query",
            "code", generateNonce(), Date().time.toDouble())
        val jwt = DMEJsonWebToken(header, paylod)
        return jwt
    }

    fun buildForPreAuthRedemption(preauthCode: String): DMEJsonWebToken {

    }

    private fun generateCodeVerifier(): String {
        val lowerBound = 43
        val upperBound = 128
        val length = SecureRandom().nextInt(upperBound - lowerBound + 1) + lowerBound

        @Suppress("SpellCheckingInspection")
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_~".split("")

        var codeVerifier = ""
        repeat(length) {
            val index = SecureRandom().nextInt(charset.count())
            val char = charset[index]
            codeVerifier += char
        }

        return codeVerifier
    }

    private fun generateCodeChallenge(verifier: String): String {
        val dgst = MessageDigest.getInstance("SHA-256")
        dgst.update(verifier.toByteArray())
        val raw = dgst.digest()
        return Base64.encodeToString(raw, Base64.DEFAULT)
    }

    private fun generateNonce(): String {
        @Suppress("SpellCheckingInspection")
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".split("")

        var nonce = ""
        repeat(32) {
            val index = SecureRandom().nextInt(charset.count())
            val char = charset[index]
            codeVerifier += char
        }

        return nonce
    }
}