package me.digi.sdk.utilities.jwt

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(PreauthorizationResponseJWT.Adapter::class)
internal class PreauthorizationResponseJWT(tokenised: String? = null): JsonWebToken(tokenised) {

    @JwtClaim
    lateinit var preauthorizationCode: String
        private set


    inner class Adapter: JsonWebToken.Adapter<PreauthorizationResponseJWT>()
}