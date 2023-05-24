package me.digi.sdk.entities.payload

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.Session

data class TokenReferencePayload(
    @SerializedName("expires_on")
    val expiresOn: Int? = 0,
    @SerializedName("reference_code")
    val referenceCode: String? = "",
    @SerializedName("token_type")
    val tokenType: String? = ""
)

data class OnboardPayload(
    val tokenReferencePayload: TokenReferencePayload? = null,
    val session: Session? = null,
    val code: String? = null
)