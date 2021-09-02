package me.digi.sdk

import android.app.Application
import android.content.Context
import me.digi.sdk.api.APIClient
import me.digi.sdk.entities.configuration.ClientConfiguration
import me.digi.sdk.interapp.AppCommunicator
import me.digi.sdk.utilities.SessionManager

abstract class Client(context: Context, config: ClientConfiguration) {

    protected val apiClient: APIClient
    protected val sessionManager: SessionManager

    init {

        if (context !is Application) {
            throw SDKError.InvalidContext()
        }

        apiClient = APIClient(context, config)
        sessionManager = SessionManager()
        AppCommunicator.initializeSharedInstance(context)
    }
}