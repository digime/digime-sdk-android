package me.digi.sdk.entities

import me.digi.sdk.utilities.jwt.DMEAuthCodeExchangeResponseJWT
import java.text.SimpleDateFormat
import java.util.*

data class DMEOAuthToken(
    val accessToken: String,
    val expiresOn: String,
    val refreshToken: String,
    val tokenType: String
) {

    internal constructor(jwt: DMEAuthCodeExchangeResponseJWT) : this(
        jwt.accessToken,
        getDateMillisecondsFromSeconds(jwt.expiresOn.toLong().toString(), "yyyy.MM.dd HH:mm"),
        jwt.refreshToken,
        jwt.tokenType
    )
}

fun getDateMillisecondsFromSeconds(time: String, dateFormat: String): String = try {
    val dateTenetFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
    val netDate = Date(time.toLong() * 1000)
    dateTenetFormat.format(netDate)
} catch (e: Exception) {
    e.toString()
}