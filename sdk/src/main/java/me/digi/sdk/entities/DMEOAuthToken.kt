package me.digi.sdk.entities

import me.digi.sdk.utilities.jwt.AuthCodeExchangeResponseJWT
import java.util.Date

data class DMEOAuthToken (

    val accessToken: String,
    val expiresOn: Date,
    val refreshToken: String,
    val tokenType: String

) {
    internal constructor(jwt: AuthCodeExchangeResponseJWT): this(jwt.accessToken, Date(jwt.expiresOn.toLong()), jwt.refreshToken, jwt.tokenType)
}