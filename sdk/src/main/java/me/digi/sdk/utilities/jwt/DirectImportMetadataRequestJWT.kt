package me.digi.sdk.utilities.jwt

import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import java.util.*

internal class DirectImportMetadataRequestJWT(
    @JwtClaim val metadata: String,
) : JsonWebToken() {

    override fun tokenize(): String = super.tokenize()
}