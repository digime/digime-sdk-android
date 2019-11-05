package me.digi.sdk.entities

sealed class DMESessionGrant(val value: String) {

    class AccessToken(value: String): DMESessionGrant(value)
    class RefreshToken(value: String): DMESessionGrant(value)
    class SessionExchangeToken(value: String): DMESessionGrant(value)
    class AuthorizationCode(value: String): DMESessionGrant(value)
}