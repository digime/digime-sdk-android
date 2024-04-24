package me.digi.sdk.entities.response

import com.google.gson.annotations.SerializedName
import me.digi.sdk.entities.payload.CredentialsPayload

data class Account(
    val id: String = "",
    val name: String = "",
    val service: Service = Service()
)

data class Service(
    val logo: String = "",
    val name: String = ""
)

data class Accounts(
    val accounts: List<Account> = listOf(),
    @SerializedName("consentid")
    val consentId: String = "",
)
data class GetAccountsResponse(
    val readAccounts: Accounts,
    val credentials: CredentialsPayload? = null
)