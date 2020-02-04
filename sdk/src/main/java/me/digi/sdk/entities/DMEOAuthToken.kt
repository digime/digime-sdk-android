package me.digi.sdk.entities

import me.digi.sdk.utilities.jwt.DMEAuthCodeExchangeResponseJWT
import java.util.*

data class DMEOAuthToken (

    val accessToken: String,
    val expiresOn: Date,
    val refreshToken: String,
    val tokenType: String

) {
    internal constructor(jwt: DMEAuthCodeExchangeResponseJWT): this(jwt.accessToken, Date(jwt.expiresOn.toLong()), jwt.refreshToken, jwt.tokenType)
}