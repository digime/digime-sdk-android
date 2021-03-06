package me.digi.sdk.utilities.jwt

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(DMEPreauthorizationResponseJWT.Adapter::class)
internal class DMEPreauthorizationResponseJWT(tokenised: String): JsonWebToken(tokenised) {

    @JwtClaim
    lateinit var preauthorizationCode: String
        private set


    inner class Adapter: JsonWebToken.Adapter<DMEPreauthorizationResponseJWT>()
}