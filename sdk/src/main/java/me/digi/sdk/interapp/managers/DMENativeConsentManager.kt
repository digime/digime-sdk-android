package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMESDKError
import me.digi.sdk.R
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.DMESessionManager
import me.digi.sdk.utilities.toMap

class DMENativeConsentManager(val sessionManager: DMESessionManager, val appId: String): DMEAppCallbackHandler() {

    fun beginAuthorization(fromActivity: Activity, completion: DMEAuthorizationCompletion) {

        val ctx = DMEAppCommunicator.getSharedInstance().context

        sessionManager.currentSession?.let { session ->

            val caParams = mapOf(
                "KEY_SESSION_TOKEN" to session.key,
                "KEY_APP_ID" to appId
            )

            val launchIntent = DMEAppCommunicator.getSharedInstance().buildIntentFor(R.string.deeplink_data, caParams)
            DMEAppCommunicator.getSharedInstance().openDigiMeApp(fromActivity, launchIntent)

        } ?: run {
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    override fun canHandle(intent: Intent): Boolean {
        val communicator = DMEAppCommunicator.getSharedInstance()
        val params = intent.extras.toMap()
        return when (intent.action) {
            DMEAppCommunicator.getSharedInstance().buildActionFor(R.string.deeplink_consent_access) -> {
                (params.containsKey(communicator.context.getString(R.string.key_app_id)) &&
                        params.containsKey(communicator.context.getString(R.string.key_contract_id)))
            }
            else -> false
        }
    }

    override fun handle(intent: Intent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}