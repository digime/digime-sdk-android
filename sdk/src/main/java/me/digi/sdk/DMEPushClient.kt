package me.digi.sdk

import android.app.Activity
import android.content.Context
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.entities.DMEPushConfiguration
import me.digi.sdk.entities.DMESDKAgent
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager
import me.digi.sdk.utilities.DMELog

class DMEPushClient(val context: Context, val configuration: DMEPushConfiguration): DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy { DMEPostboxConsentManager(sessionManager, configuration.appId) }

    fun createPostbox(fromActivity: Activity, completion: DMEPostboxCreationCompletion) {

        DMELog.i("Launching user consent request.")

        val req = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", null)
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                postboxConsentManager.beginPostboxAuthorization(fromActivity, completion)
            }
            else {
                DMELog.e("An error occurred whilst communicating with our servers: ${error?.message}")
                completion(null, error)
            }
        }
    }
}