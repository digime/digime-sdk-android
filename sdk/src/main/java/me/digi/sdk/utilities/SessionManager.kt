package me.digi.sdk.utilities

import me.digi.sdk.entities.Session
import me.digi.sdk.entities.request.SessionRequest
import me.digi.sdk.entities.response.SessionResponse
import java.util.*

class SessionManager {

    var currentSession: SessionResponse? = null
    var updatedSession: Session? = null

    fun isSessionValid() = updatedSession?.let {
        Date(it.expiry).after(Date())
    } ?: false
}