package me.digi.sdk

import android.app.Activity
import android.content.Context
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.entities.DMEPushClientConfiguration
import me.digi.sdk.entities.DMESDKAgent
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager

class DMEPushClient(val context: Context, val configuration: DMEPushClientConfiguration): DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy { DMEPostboxConsentManager(sessionManager, configuration.appId) }

    fun createPostbox(fromActivity: Activity, completion: DMEPostboxCreationCompletion) {

        val req = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", null)
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                postboxConsentManager.beginPostboxAuthorization(fromActivity, completion)
            }
            else {
                completion(null, error)
            }
        }
    }
}