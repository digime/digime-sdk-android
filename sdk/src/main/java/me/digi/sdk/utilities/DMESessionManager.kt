package me.digi.sdk.utilities

import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import java.util.Date

class DMESessionManager(private val apiClient: DMEAPIClient, private val clientConfig: DMEClientConfiguration) {

    var currentSession: DMESession? = null
    var currentScope: DMESessionRequest? = null

    fun getSession(sessionRequest: DMESessionRequest, completion: DMEAuthorizationCompletion) {

        currentSession = null
        currentScope = null

        apiClient.makeCall(apiClient.argonService.getSession(sessionRequest)) { session, error ->

            session?.scope = sessionRequest.scope
            session?.createdDate = Date()
            currentSession = session
            currentScope = sessionRequest
            completion(session, error)
        }
    }

    fun isSessionValid() = currentSession?.let {
        it.expiryDate.after(Date())
    } ?: false

    fun isSessionKeyValid(key: String) = currentSession?.let {
        key.isNotEmpty() && key == it.key
    } ?: false
}