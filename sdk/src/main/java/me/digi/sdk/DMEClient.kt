package me.digi.sdk

import android.app.Application
import android.content.Context
import android.content.Intent
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.DMEDeepLinkBuilder
import me.digi.sdk.utilities.DMESessionManager

abstract class DMEClient(private val context: Context, private val config: DMEClientConfiguration) {

    protected val apiClient: DMEAPIClient
    protected val sessionManager: DMESessionManager

    init {

        if (context !is Application) {
            throw DMESDKError.InvalidContext()
        }

        apiClient = DMEAPIClient(context, config)
        sessionManager = DMESessionManager(apiClient, config)
        DMEAppCommunicator.initializeSharedInstance(context)
    }

    fun canOpenDigiMeApp() = DMEAppCommunicator(context).canOpenDMEApp()

    fun viewReceiptsInDigiMeApp() {

        if (!canOpenDigiMeApp())
            throw DMESDKError.DigiMeAppNotFound()

        val launchURI = DMEDeepLinkBuilder()
            .setAction("receipt")
            .addParameter(context.getString(R.string.key_contract_id), config.contractId)
            .addParameter(context.getString(R.string.key_app_id), config.appId)
            .build()

        val launchIntent = Intent(Intent.ACTION_VIEW, launchURI)

        context.startActivity(launchIntent)
    }
}