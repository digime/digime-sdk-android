package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import me.digi.sdk.DMEAuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.DMESessionManager
import me.digi.sdk.utilities.toMap

class DMEPostboxConsentManager(val sessionManager: DMESessionManager, val appId: String): DMEAppCallbackHandler() {

    private var pendingPostboxCallbackHandler: DMEPostboxCreationCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(null, DMEAuthError.Cancelled())
            }
            field = value
        }

    fun beginPostboxAuthorization(fromActivity: Activity, completion: DMEPostboxCreationCompletion) {

        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)

        val ctx = DMEAppCommunicator.getSharedInstance().context

        sessionManager.currentSession?.let { session ->

            val caParams = mapOf(
                ctx.getString(R.string.key_session_key) to session.key,
                ctx.getString(R.string.key_app_id) to appId
            )

            val launchIntent = DMEAppCommunicator.getSharedInstance().buildIntentFor(R.string.deeplink_create_postbox, caParams)
            pendingPostboxCallbackHandler = completion
            DMEAppCommunicator.getSharedInstance().openDigiMeApp(fromActivity, launchIntent)

        } ?: run {
            DMELog.e("Your session is invalid, please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data:Intent?): Boolean {
        val communicator = DMEAppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_create_postbox))
    }

    override fun handle(intent: Intent?) {
        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the consent request activity.")
            pendingPostboxCallbackHandler?.invoke(null, DMEAuthError.General())
            DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
            pendingPostboxCallbackHandler = null
            return
        }

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()
        val result = params[ctx.getString(R.string.key_result)] as? String
        val postboxId = params[ctx.getString(R.string.key_postbox_id)] as? String
        val postboxPublicKey = params[ctx.getString(R.string.key_public_key)] as? String
        val sessionKey = params[ctx.getString(R.string.key_session_key)] as? String

        extractAndAppendMetadata(params)

        val error = if (!sessionManager.isSessionValid()) {
            DMEAuthError.InvalidSession()
        }
        else if (postboxId == null || postboxPublicKey == null || sessionKey == null) {
            DMEAuthError.General()
        }
        else when (result) {
            ctx.getString(R.string.const_result_error) -> {
                DMELog.e("There was a problem requesting consent.")
                DMEAuthError.General()
            }
            ctx.getString(R.string.const_result_cancel) -> {
                DMELog.e("User rejected consent request.")
                DMEAuthError.Cancelled()
            }
            else -> {
                DMELog.i("User accepted consent request; postbox created.")
                null
            }
        }

        val postbox = DMEPostbox(sessionKey!!, postboxId!!, postboxPublicKey!!)
        pendingPostboxCallbackHandler?.invoke(postbox, error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        pendingPostboxCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) = sessionManager.currentSession?.let { session ->

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val metadataWhitelistedKeys = listOf(

            // Functional Keys
            R.string.key_postbox_id,
            R.string.key_public_key,
            R.string.key_result,
            R.string.key_app_id

        ).map { ctx.getString(it) }

        session.metadata.putAll(payload.filter {
            metadataWhitelistedKeys.contains(it.key)
        })

    } ?: run { Unit }
}