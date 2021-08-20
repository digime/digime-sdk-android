package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import me.digi.sdk.AuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.PostboxCreationCompletion
import me.digi.sdk.entities.Data
import me.digi.sdk.interapp.AppCallbackHandler
import me.digi.sdk.interapp.AppCommunicator
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.SessionManager
import me.digi.sdk.utilities.toMap

class PostboxConsentManager(val sessionManager: SessionManager, val appId: String): AppCallbackHandler() {

    private var pendingPostboxCallbackHandler: PostboxCreationCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(null, AuthError.Cancelled)
            }
            field = value
        }

    fun beginPostboxAuthorization(fromActivity: Activity, completion: PostboxCreationCompletion) {

        AppCommunicator.getSharedInstance().addCallbackHandler(this)

        val ctx = AppCommunicator.getSharedInstance().context

        sessionManager.currentSession?.let { session ->

            val caParams = mapOf(
                ctx.getString(R.string.key_session_key) to session.key,
                ctx.getString(R.string.key_app_id) to appId
            )

            val launchIntent = AppCommunicator.getSharedInstance().buildIntentFor(R.string.deeplink_create_postbox, caParams)
            pendingPostboxCallbackHandler = completion
            AppCommunicator.getSharedInstance().openDigiMeApp(fromActivity, launchIntent)

        } ?: run {
            DMELog.e("Your session is invalid, please request a new one.")
            completion(null, AuthError.InvalidSession())
        }
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data:Intent?): Boolean {
        val communicator = AppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_create_postbox))
    }

    override fun handle(intent: Intent?) {
        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the consent request activity.")
            pendingPostboxCallbackHandler?.invoke(null, AuthError.General())
            AppCommunicator.getSharedInstance().removeCallbackHandler(this)
            pendingPostboxCallbackHandler = null
            return
        }

        val ctx = AppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()
        val result = params[ctx.getString(R.string.key_result)] as? String
        val postboxId = params[ctx.getString(R.string.key_postbox_id)] as? String
        val postboxPublicKey = params[ctx.getString(R.string.key_public_key)] as? String
        val sessionKey = params[ctx.getString(R.string.key_session_key)] as? String
        val digiMeVersion = params[ctx.getString(R.string.key_app_version)] as? String

        extractAndAppendMetadata(params as Map<String, Any>)

        val error = if (!sessionManager.isSessionValid()) {
            AuthError.InvalidSession()
        }
        else if (postboxId == null || postboxPublicKey == null || sessionKey == null) {
            AuthError.General()
        }
        else when (result) {
            ctx.getString(R.string.const_result_error) -> {
                DMELog.e("There was a problem requesting consent.")
                AuthError.General()
            }
            ctx.getString(R.string.const_result_cancel) -> {
                DMELog.e("User rejected consent request.")
                AuthError.Cancelled
            }
            else -> {
                DMELog.i("User accepted consent request; postbox created.")
                null
            }
        }

        val postbox = Data(sessionKey!!, postboxId!!, postboxPublicKey!!)
        pendingPostboxCallbackHandler?.invoke(postbox, error)
        AppCommunicator.getSharedInstance().removeCallbackHandler(this)
        pendingPostboxCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) = sessionManager.currentSession?.let { session ->

        val ctx = AppCommunicator.getSharedInstance().context

        val metadataWhitelistedKeys = listOf(

            // Functional Keys
            R.string.key_postbox_id,
            R.string.key_public_key,
            R.string.key_result,
            R.string.key_app_id,
            R.string.key_app_version

        ).map { ctx.getString(it) }

        session.metadata.putAll(payload.filter {
            metadataWhitelistedKeys.contains(it.key)
        })

    } ?: run { Unit }
}