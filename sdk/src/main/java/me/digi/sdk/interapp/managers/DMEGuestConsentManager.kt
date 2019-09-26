package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.digi.sdk.DMEAuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.ui.GuestConsentBrowserActivity
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.DMESessionManager
import me.digi.sdk.utilities.toMap

class DMEGuestConsentManager(private val sessionManager: DMESessionManager, private val baseURL: String): DMEAppCallbackHandler() {

    private var pendingAuthCallbackHandler: DMEAuthorizationCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(null, DMEAuthError.Cancelled())
            }
            field = value
        }

    fun beginGuestAuthorization(fromActivity: Activity, completion: DMEAuthorizationCompletion) {

        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)
        pendingAuthCallbackHandler = completion

        val guestRequestCode = DMEAppCommunicator.getSharedInstance().requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)
        val proxyLaunchIntent = Intent(fromActivity, GuestConsentBrowserActivity::class.java)
        proxyLaunchIntent.setData(buildQuarkURI())

        fromActivity.startActivityForResult(proxyLaunchIntent, guestRequestCode)
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data:Intent?): Boolean {
        val communicator = DMEAppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback))
    }

    override fun handle(intent: Intent?) {

        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the guest consent browser activity.")
            pendingAuthCallbackHandler?.invoke(sessionManager.currentSession, DMEAuthError.General())
            DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
            pendingAuthCallbackHandler = null
            return
        }

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()
        val result = params[ctx.getString(R.string.key_result)] as? String

        extractAndAppendMetadata(params)

        val error = if (!sessionManager.isSessionValid()) {
            DMEAuthError.InvalidSession()
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
                DMELog.i("User accepted consent request.")
                null
            }
        }

        pendingAuthCallbackHandler?.invoke(sessionManager.currentSession, error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        pendingAuthCallbackHandler = null

    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        // TODO: Does quark do metadata?
    }

    private fun buildQuarkURI(): Uri {

        val session = sessionManager.currentSession ?: throw DMEAuthError.InvalidSession()
        val ctx = DMEAppCommunicator.getSharedInstance().context

        val exchangeTokenKey = ctx.getString(R.string.key_session_exchange_token)?.orEmpty()
        val callbacklUrlKey = ctx.getString(R.string.key_callback_url)?.orEmpty()
        val callbackUrl = ctx.getString(R.string.deeplink_guest_consent_callback)?.orEmpty() + "://?"

        return Uri.parse("${baseURL}apps/quark/v1/direct-onboarding")
            .buildUpon()
            .appendQueryParameter(exchangeTokenKey, session.exchangeToken)
            .appendQueryParameter(callbacklUrlKey, callbackUrl)
            .build()
    }
}