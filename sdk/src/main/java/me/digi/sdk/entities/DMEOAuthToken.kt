package me.digi.sdk.entities

import java.util.*

data class DMEOAuthToken (

    val accessToken: String,
    val expiresOn: Date,
    val refreshToken: String,
    val tokenType: String

)