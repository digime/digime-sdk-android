package me.digi.sdk.entities

import com.google.gson.annotations.SerializedName

data class AccountList (

    @SerializedName("accounts")
    val accounts: List<Account>?,

    @SerializedName("consentid")
    val consentid: String
)