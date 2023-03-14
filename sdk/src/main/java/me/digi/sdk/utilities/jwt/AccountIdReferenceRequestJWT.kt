package me.digi.sdk.utilities.jwt

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.TokenReferencePayload
import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import java.util.*

@Suppress("UNUSED")
internal class AccountIdReferenceRequestJWT(
    appId: String,
    contractId: String
) : JsonWebToken() {

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

data class AccountIdReferencePayload(
    val tokenReferencePayload: TokenReferencePayload? = null,
    val session: Session? = null,
    val code: String? = null,
    val accountId: String? = null
)