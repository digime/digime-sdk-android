package me.digi.sdk.utilities.jwt

import com.google.gson.annotations.JsonAdapter
import java.util.*

@JsonAdapter(AuthCodeExchangeResponseJWT.Adapter::class)
internal class AuthCodeExchangeResponseJWT(tokenised: String): JsonWebToken(tokenised) {

    @JwtClaim
    lateinit var accessToken: String
        private set

    @JwtClaim
    lateinit var refreshToken: String
        private set

    @JwtClaim
    var expiresOn: Double = 0.0
        private set

    @JwtClaim
    lateinit var tokenType: String
        private set

    inner class Adapter: JsonWebToken.Adapter<AuthCodeExchangeResponseJWT>()
}