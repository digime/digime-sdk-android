package me.digi.sdk.entities

data class AuthorizationScopeRequest(
    val actions: Actions = Actions(),
    val agent: Agent = Agent()
)

data class Actions(
    val pull: Pull = Pull()
)

data class Pull(
    val scope: DataRequest? = null
)

data class Agent(
    val sdk: SdkAgent = SdkAgent()
)