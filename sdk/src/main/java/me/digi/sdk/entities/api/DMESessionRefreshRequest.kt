package me.digi.sdk.entities.api

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.DMESDKAgent

data class DMESessionRefreshRequest(

    val refreshToken: String,

    val sdkAgent: DMESDKAgent,

    @SerializedName("accept")
    val compression: String,

    val scope: DMEDataRequest

)