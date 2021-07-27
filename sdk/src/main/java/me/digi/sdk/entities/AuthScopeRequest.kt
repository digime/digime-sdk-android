package me.digi.sdk.entities

data class AuthorizationScopeRequest(
    val actions: Actions = Actions(),
    val agent: Agent = Agent()
)

data class Actions(
    val pull: Pull = Pull()
)

data class Pull(
    val scope: DMEDataRequest? = null
)

data class Agent(
    val sdk: DMESDKAgent = DMESDKAgent()
)