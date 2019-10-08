package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class DMEAccountList (

    @SerializedName("accounts")
    val accounts: List<DMEAccount>?,

    @SerializedName("consentid")
    val consentid: String
)