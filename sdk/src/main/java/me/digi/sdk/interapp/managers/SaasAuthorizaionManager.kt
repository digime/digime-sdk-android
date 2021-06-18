package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.digi.sdk.DMEAuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.AuthorizationCompletion
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.ui.GuestConsentBrowserActivity
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.toMap

class SaasAuthorizaionManager(private val baseURL: String): DMEAppCallbackHandler() {

    private var authorizationCallbackHandler: AuthorizationCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(null, DMEAuthError.Cancelled())
            }
            field = value
        }

    fun beginConsentAction(fromActivity: Activity, codeValue: String, completion: AuthorizationCompletion) {
        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)
        authorizationCallbackHandler = completion

        val guestRequestCode = DMEAppCommunicator.getSharedInstance().requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)

        val proxyLaunchIntent = Intent(fromActivity, GuestConsentBrowserActivity::class.java)

        proxyLaunchIntent.data = buildSaaSClientURI(codeValue)

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
            DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
            authorizationCallbackHandler = null
            return
        }

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()

        val code = params["code"] as? String
        val state = params["state"] as? String
        val success = params["success"] as? String
        val errorCode = params["error"] as? String
        val result = params["result"] as? String

        var error: DMEAuthError? =  null

        when (result) {
            ctx.getString(R.string.const_result_error) -> {
                DMELog.e("There was a problem requesting consent.")
                error = DMEAuthError.General()
            }
            ctx.getString(R.string.const_result_cancel) -> {
                DMELog.e("User rejected consent request.")
                error = DMEAuthError.Cancelled()
            }
            else -> {
                DMELog.i("User accepted consent request.")
            }
        }

        authorizationCallbackHandler?.invoke(AuthSession(code, state), error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        authorizationCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        // TODO: Does quark do metadata?
    }

    private fun buildSaaSClientURI(codeValue: String): Uri {

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val code = ctx.getString(R.string.saas_client_code)

        return Uri.parse("${baseURL}apps/saas/authorize")
            .buildUpon()
            .appendQueryParameter(code, codeValue)
            .appendQueryParameter("callback", "http://www.digi.me/return")
            .build()
    }
}