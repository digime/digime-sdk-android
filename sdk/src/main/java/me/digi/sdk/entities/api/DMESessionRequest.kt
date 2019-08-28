package me.digi.sdk.entities.api

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.DMESDKAgent

data class DMESessionRequest (

    @SerializedName("appId")
    val appId: String,

    @SerializedName("contractId")
    val contractId: String,

    @SerializedName("sdkAgent")
    val sdkAgent: DMESDKAgent,

    @SerializedName("accept")
    val compression: String,

    @SerializedName("scope")
    val scope: DMEDataRequest?
)