package me.digi.sdk.entities.response

import com.google.gson.annotations.SerializedName

data class Account(
    val id: String = "",
    val name: String = "",
    val service: Service = Service()
)

data class Service(
    val logo: String = "",
    val name: String = ""
)

data class ReadAccountsResponse(
    val accounts: List<Account> = listOf(),
    @SerializedName("consentid")
    val consentId: String = ""
)