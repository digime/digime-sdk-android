package me.digi.sdk.utilities

import me.digi.sdk.api.APIClient
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.configuration.ClientConfiguration
import me.digi.sdk.entities.request.SessionRequest
import me.digi.sdk.entities.response.SessionResponse
import java.util.*

class SessionManager {

    var currentSession: SessionResponse? = null
    var updatedSession: Session? = null
    var currentScope: SessionRequest? = null

    fun isSessionValid() = updatedSession?.let {
        Date(it.expiry).after(Date())
    } ?: false

    fun isSessionKeyValid(key: String) = currentSession?.let {
        key.isNotEmpty() && key == it.key
    } ?: false
}