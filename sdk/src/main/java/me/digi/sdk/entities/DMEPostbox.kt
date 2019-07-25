package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEPostbox (

    @SerializedName("sessionkey")
    val sessionKey: String,

    @SerializedName("postboxid")
    val postboxId: String,

    @SerializedName("publickey")
    val publicKey: String

)