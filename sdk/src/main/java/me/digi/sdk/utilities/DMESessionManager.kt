package me.digi.sdk.utilities

import kotlinx.coroutines.launch
import me.digi.sdk.DMEError
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.callbacks.DMEAuthorizationCallback
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import java.util.Date

internal class DMESessionManager(private val apiClient: DMEAPIClient, private val clientConfig: DMEClientConfiguration) {

    var currentSession: DMESession? = null
    var currentScope: DMESessionRequest? = null

    fun getSession(sessionRequest: DMESessionRequest, completion: DMEAuthorizationCallback) {

        currentSession = null
        currentScope = null

        apiClient.makeCall(apiClient.argonService.getSession(sessionRequest)) { session, error ->

            currentSession = session
            currentScope = sessionRequest
            completion(session, error)
        }
    }

    fun isSessionValid() = currentSession?.let {
        it.expiryDate.after(Date()) && it.key == clientConfig.contractId
    } ?: false

    fun isSessionKeyValid(key: String) = currentSession?.let {
        key.isNotEmpty() && key == it.key
    } ?: false
}