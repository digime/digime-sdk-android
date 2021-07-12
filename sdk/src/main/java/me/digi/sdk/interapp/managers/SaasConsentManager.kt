package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.digi.sdk.DMEAuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.AuthorizationCompletion
import me.digi.sdk.callbacks.OnboardingCompletion
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.ui.GuestConsentBrowserActivity
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.toMap

/**
 * Update to reflect entire Saas handler
 * Return different callback handlers
 */
class SaasConsentManager(private val baseURL: String, private val type: String) :
    DMEAppCallbackHandler() {

    private var authorizationCallbackHandler: AuthorizationCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(null, DMEAuthError.Cancelled())
            }
            field = value
        }

    private var onboardingCallbackHandler: OnboardingCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(DMEAuthError.Cancelled())
            }
            field = value
        }

    fun beginConsentAction(
        fromActivity: Activity,
        codeValue: String,
        serviceId: String? = null,
        completion: AuthorizationCompletion
    ) {
        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)
        authorizationCallbackHandler = completion
        handlerAction(fromActivity, serviceId, codeValue)
    }

    fun beginOnboardAction(
        fromActivity: Activity,
        codeValue: String,
        serviceId: String? = null,
        completion: OnboardingCompletion
    ) {
        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)
        onboardingCallbackHandler = completion
        handlerAction(fromActivity, serviceId, codeValue)
    }

    private fun handlerAction(fromActivity: Activity, serviceId: String?, codeValue: String) {
        val guestRequestCode = DMEAppCommunicator.getSharedInstance()
            .requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)

        val proxyLaunchIntent = Intent(fromActivity, GuestConsentBrowserActivity::class.java)

        proxyLaunchIntent.data = buildSaaSClientURI(serviceId, codeValue)

        fromActivity.startActivityForResult(proxyLaunchIntent, guestRequestCode)
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data: Intent?): Boolean {
        val communicator = DMEAppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback))
    }

    override fun handle(intent: Intent?) {

        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the guest consent browser activity.")
            DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
            authorizationCallbackHandler = null
            onboardingCallbackHandler = null
            return
        }

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()

        val code = params["code"] as? String
        val state = params["state"] as? String
        val postboxId = params["postboxId"] as? String
        val publicKey = params["publicKey"] as? String
        val success = params["success"] as? String
        val errorCode = params["error"] as? String
        val result = params["result"] as? String

        var error: DMEAuthError? = null

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

        authorizationCallbackHandler?.invoke(AuthSession(code, state, postboxId, publicKey), error)
        onboardingCallbackHandler?.invoke(error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        authorizationCallbackHandler = null
        onboardingCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        // TODO: Does quark do metadata?
    }

    private fun buildSaaSClientURI(serviceId: String? = null, codeValue: String): Uri {

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val code = ctx.getString(R.string.saas_client_code)

        val uri: Uri.Builder = Uri.parse("${baseURL}apps/saas/$type")
            .buildUpon()
            .appendQueryParameter(code, codeValue)
            .appendQueryParameter("callback", "digime-ca://auth")

        serviceId?.let { uri.appendQueryParameter("service", serviceId) }

        return uri.build()
    }
}