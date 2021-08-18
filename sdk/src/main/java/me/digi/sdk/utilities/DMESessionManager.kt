package me.digi.sdk.utilities

import me.digi.sdk.api.APIClient
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.configuration.ClientConfiguration
import me.digi.sdk.entities.request.DMESessionRequest
import me.digi.sdk.entities.response.SessionResponse
import java.util.*

class DMESessionManager(
    private val apiClient: APIClient,
    private val clientConfig: ClientConfiguration
) {

    var currentSession: SessionResponse? = null
    var updatedSession: Session? = null
    var currentScope: DMESessionRequest? = null

    fun getSession(sessionRequest: DMESessionRequest, completion: DMEAuthorizationCompletion) {

        currentSession = null
        currentScope = null

        apiClient.makeCall(apiClient.argonService.getSession(sessionRequest)) { session, error ->

            session?.scope = sessionRequest.scope
            session?.createdDate = Date()
            session?.metadata = emptyMap<String, Any>().toMutableMap()
            currentSession = session
            currentScope = sessionRequest
            completion(session, error)
        }
    }

    fun isSessionValid() = updatedSession?.let {
        Date(it.expiry).after(Date())
    } ?: false

    fun isSessionKeyValid(key: String) = currentSession?.let {
        key.isNotEmpty() && key == it.key
    } ?: false
}