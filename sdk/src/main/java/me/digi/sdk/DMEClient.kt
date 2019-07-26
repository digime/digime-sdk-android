package me.digi.sdk

import android.content.Context
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.utilities.DMESessionManager

internal abstract class DMEClient(private val context: Context, private val config: DMEClientConfiguration) {

    private val apiClient: DMEAPIClient
    private val sessionManager: DMESessionManager


    init {
        apiClient = DMEAPIClient(context, config)
        sessionManager = DMESessionManager(apiClient, config)
    }
}