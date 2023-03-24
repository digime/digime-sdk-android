package me.digi.sdk.entities.request

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.SdkAgent

data class AccountIdRequest (
    val type: String = "accountId",
    var value: String? = null
)