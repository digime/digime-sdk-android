package me.digi.sdk

import android.content.Context
import kotlinx.coroutines.launch
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.callbacks.DMEAccountsCompletion
import me.digi.sdk.callbacks.DMEFileContentCompletion
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.utilities.DMESessionManager

internal abstract class DMEClient(private val context: Context, private val config: DMEClientConfiguration) {

    private val apiClient: DMEAPIClient
    private val sessionManager: DMESessionManager


    init {
        apiClient = DMEAPIClient(context, config)
        sessionManager = DMESessionManager(apiClient, config)
    }

    fun getSessionData(downloadHandler: DMEFileContentCompletion, completion: (DMEError?) -> Unit) {

    }

    fun getSessionData(fileId: String, completion: DMEFileContentCompletion) {

    }

    fun getSessionAccounts(completion: DMEAccountsCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            apiClient.makeCall(apiClient.argonService.getAccounts(currentSession.key), completion)

        }
        else {
            completion(null, DMEAuthError.InvalidSession())
        }

    }
}