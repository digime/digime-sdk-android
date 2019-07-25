package me.digi.sdk.utilities

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.digi.sdk.DMEError
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.callbacks.DMEAuthorizationCallback
import me.digi.sdk.entities.DMEDataRequest
import me.digi.sdk.entities.DMEScope
import me.digi.sdk.entities.DMESession
import me.digi.sdk.entities.api.DMESessionRequest
import java.util.Date

internal class DMESessionManager(private val apiClient: DMEAPIClient) {

    var currentSession: DMESession? = null
    var scope: DMEDataRequest = DMEScope() // Default to entire scope.

    fun getSession(sessionRequest: DMESessionRequest, completion: DMEAuthorizationCallback) {

        currentSession = null

        apiClient.sharedAPIScope.launch {
            try {

                val session = apiClient.argonService.getSession(sessionRequest)
                currentSession = session
                completion(session, null)

            } catch (error: DMEError) {

                completion(null, error)

            }
        }
    }

    fun isSessionValid(): Boolean {
        return currentSession?.let {
            it.expiryDate.after(Date()) && it.id
        } ?: false
    }
}