package me.digi.sdk

import android.app.Application
import android.content.Context
import android.content.Intent
import me.digi.sdk.api.APIClient
import me.digi.sdk.entities.configuration.ClientConfiguration
import me.digi.sdk.interapp.AppCommunicator
import me.digi.sdk.utilities.DeepLinkBuilder
import me.digi.sdk.utilities.SessionManager

abstract class Client(private val context: Context, private val config: ClientConfiguration) {

    protected val apiClient: APIClient
    protected val sessionManager: SessionManager

    init {

        if (context !is Application) {
            throw SDKError.InvalidContext()
        }

        apiClient = APIClient(context, config)
        sessionManager = SessionManager(apiClient, config)
        AppCommunicator.initializeSharedInstance(context)
    }

    fun canOpenDigiMeApp() = AppCommunicator(context).canOpenApp()

    fun viewReceiptsInDigiMeApp() {

        if (!canOpenDigiMeApp())
            throw SDKError.DigiMeAppNotFound()

        val launchURI = DeepLinkBuilder()
            .setAction("receipt")
            .addParameter(context.getString(R.string.key_contract_id), config.contractId)
            .addParameter(context.getString(R.string.key_app_id), config.appId)
            .build()

        val launchIntent = Intent(Intent.ACTION_VIEW, launchURI)

        context.startActivity(launchIntent)
    }

    fun isSessionValid() = sessionManager.isSessionValid()
}