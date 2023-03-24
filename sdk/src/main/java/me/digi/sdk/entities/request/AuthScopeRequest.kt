package me.digi.sdk.entities.request

import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.SdkAgent

data class AuthorizationScopeRequest(
    val actions: Actions = Actions(),
    val agent: Agent = Agent()
)

data class Actions(val pull: Pull = Pull())

data class Pull(
    val scope: DataRequest? = null,
    val accept: Accept = Accept()
)

data class Accept(val compression: String = "gzip")

data class Agent(val sdk: SdkAgent = SdkAgent())