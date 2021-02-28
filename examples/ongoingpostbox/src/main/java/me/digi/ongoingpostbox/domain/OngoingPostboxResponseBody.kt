package me.digi.ongoingpostbox.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OngoingPostboxResponseBody(
    val sessionKey: String? = "",
    val postboxId: String? = "",
    val publicKey: String? = "",
    val digiMeVersion: String? = "",
    val accessToken: String? = "",
    val expiresOn: String? = "",
    val refreshToken: String? = "",
    val tokenType: String? = ""
): Parcelable
