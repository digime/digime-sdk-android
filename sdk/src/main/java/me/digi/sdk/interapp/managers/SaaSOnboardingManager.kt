package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.digi.sdk.DMEAuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.OnboardingCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.ui.GuestConsentBrowserActivity
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.toMap

class SaaSOnboardingManager(private val baseURL: String): DMEAppCallbackHandler() {

    private var onboardingCallbackHandler: OnboardingCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(DMEAuthError.Cancelled())
            }
            field = value
        }

    fun beginOnboardAction(fromActivity: Activity, completion: OnboardingCompletion, serviceId: String, codeValue: String) {
        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)
        onboardingCallbackHandler = completion

        val guestRequestCode = DMEAppCommunicator.getSharedInstance().requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)

        val proxyLaunchIntent = Intent(fromActivity, GuestConsentBrowserActivity::class.java)

        proxyLaunchIntent.data = buildSaaSClientOnboardURI(serviceId, codeValue)

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
            onboardingCallbackHandler = null
            return
        }



        val ctx = DMEAppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()

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

        onboardingCallbackHandler?.invoke(error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        onboardingCallbackHandler = null

    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        // TODO: Does quark do metadata?
    }

    private fun buildSaaSClientOnboardURI(serviceId: String, codeValue: String): Uri {

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val code = ctx.getString(R.string.saas_client_code)
        val errorCallbackUrl = ctx.getString(R.string.saas_errorCallback)
        val successCallbackUrl = ctx.getString(R.string.saas_successCallback)

        return Uri.parse("${baseURL}apps/saas/onboard")
            .buildUpon()
            .appendQueryParameter(code, codeValue)
            .appendQueryParameter(errorCallbackUrl, "digime-ca://onboarding-failed")
            .appendQueryParameter(successCallbackUrl, "digime-ca://onboarding-success")
            .appendQueryParameter("service", serviceId)
            .build()
    }

}