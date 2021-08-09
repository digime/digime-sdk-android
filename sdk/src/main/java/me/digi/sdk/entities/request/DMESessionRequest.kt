package me.digi.sdk.entities.request

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.SdkAgent

data class DMESessionRequest (

    @SerializedName("appId")
    val appId: String,

    @SerializedName("contractId")
    val contractId: String,

    @SerializedName("sdkAgent")
    val sdkAgent: SdkAgent,

    @SerializedName("accept")
    val compression: String,

    @SerializedName("scope")
    val scope: DataRequest?
)