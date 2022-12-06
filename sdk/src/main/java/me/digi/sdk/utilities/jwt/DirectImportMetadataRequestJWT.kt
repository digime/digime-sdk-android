package me.digi.sdk.utilities.jwt

import me.digi.sdk.entities.WriteMetadata

internal class DirectImportMetadataRequestJWT(
    @JwtClaim val metadata: WriteMetadata,
) : JsonWebToken() {

    override fun tokenize(): String = super.tokenize()
}