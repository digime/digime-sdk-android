package me.digi.sdk.entities.api

import com.google.gson.annotations.SerializedName

data class DMESessionRequest (

    @SerializedName("appId")
    val appId: String,

    @SerializedName("contractId")
    val contractId: String,

    @SerializedName("sdkAgent")
    val sdkAgent: String,

    @SerializedName("accept")
    val compression: String

)