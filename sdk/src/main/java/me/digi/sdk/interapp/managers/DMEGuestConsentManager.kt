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

    fun beginConsentAction(fromActivity: Activity, completion: DMEAuthorizationCompletion, codeValue: String, appId: String) {
        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)
        pendingAuthCallbackHandler = completion

        val guestRequestCode = DMEAppCommunicator.getSharedInstance().requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)
        val proxyLaunchIntent = Intent(fromActivity, GuestConsentBrowserActivity::class.java)
        proxyLaunchIntent.setData(buildSaaSClientURI(codeValue, appId))

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

    private fun buildSaaSClientURI(codeValue: String, appId: String): Uri {

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val code = ctx.getString(R.string.saas_client_code)
        val errorCallbackUrl = ctx.getString(R.string.saas_errorCallback)
        val successCallbackUrl = ctx.getString(R.string.saas_successCallback)

        return Uri.parse("${baseURL}apps/saas/authorize")
            .buildUpon()
            .appendQueryParameter(code, codeValue)
            .appendQueryParameter(errorCallbackUrl, "digime-ca-$appId")
            .appendQueryParameter(successCallbackUrl, "digime-ca-$appId" + "://auth-success")
            .build()
    }
}