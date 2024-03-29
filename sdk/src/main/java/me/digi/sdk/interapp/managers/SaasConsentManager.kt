package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.digi.sdk.AuthError
import me.digi.sdk.R
import me.digi.sdk.callbacks.AuthorizationCompletion
import me.digi.sdk.callbacks.ServiceOnboardingCompletion
import me.digi.sdk.entities.response.ConsentAuthResponse
import me.digi.sdk.interapp.AppCallbackHandler
import me.digi.sdk.interapp.AppCommunicator
import me.digi.sdk.ui.ConsentBrowserActivity
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.toMap

class SaasConsentManager(private val baseURL: String?, private val type: String) :
    AppCallbackHandler() {

    private var authorizationCallbackHandler: AuthorizationCompletion? = null
        set(value) {
            if (field != null && value != null)
                field?.invoke(null, AuthError.Cancelled)

            field = value
        }

    private var onboardingCallbackHandler: ServiceOnboardingCompletion? = null
        set(value) {
            if (field != null && value != null)
                field?.invoke(AuthError.Cancelled)

            field = value
        }

    fun beginConsentAction(
        fromActivity: Activity,
        codeValue: String,
        serviceId: String? = null,
        completion: AuthorizationCompletion
    ) {
        AppCommunicator.getSharedInstance().addCallbackHandler(this)
        authorizationCallbackHandler = completion
        handlerAction(fromActivity, serviceId, codeValue)
    }

    fun beginOnboardAction(
        fromActivity: Activity,
        codeValue: String,
        serviceId: String? = null,
        completion: ServiceOnboardingCompletion
    ) {
        AppCommunicator.getSharedInstance().addCallbackHandler(this)
        onboardingCallbackHandler = completion
        handlerAction(fromActivity, serviceId, codeValue)
    }

    private fun handlerAction(fromActivity: Activity, serviceId: String?, codeValue: String) {
        val guestRequestCode = AppCommunicator.getSharedInstance()
            .requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)

        val proxyLaunchIntent = Intent(fromActivity, ConsentBrowserActivity::class.java)

        proxyLaunchIntent.data = buildSaaSClientURI(serviceId, codeValue)

        fromActivity.startActivityForResult(proxyLaunchIntent, guestRequestCode)
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data: Intent?): Boolean {
        val communicator = AppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback))
    }

    override fun handle(intent: Intent?) {

        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the guest consent browser activity.")
            AppCommunicator.getSharedInstance().removeCallbackHandler(this)
            authorizationCallbackHandler = null
            onboardingCallbackHandler = null
            return
        }

        val ctx = AppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()

        val success = params[ctx.getString(R.string.key_success)] as? String
        val code = params[ctx.getString(R.string.key_code)] as? String
        val state = params[ctx.getString(R.string.key_state)] as? String
        val postboxId = params[ctx.getString(R.string.key_s_postbox_id)] as? String
        val publicKey = params[ctx.getString(R.string.key_s_public_key)] as? String
        val errorCode = params[ctx.getString(R.string.key_error)] as? String

        var error: AuthError? = null

        when (errorCode) {
            ctx.getString(R.string.error_check_fail) -> {
                DMELog.e("Parameters passed in didn't pass initial checks.")
                error = AuthError.InitCheck
            }
            ctx.getString(R.string.error_invalid_code) -> {
                DMELog.e("Code passed in was not valid.")
                error = AuthError.InvalidCode
            }
            ctx.getString(R.string.error_onboard) -> {
                DMELog.e("There was an error when trying to onboard the given service.")
                error = AuthError.Onboard
            }
            ctx.getString(R.string.error_user_cancel) -> {
                DMELog.e("User rejected consent request.")
                error = AuthError.Cancelled
            }
            ctx.getString(R.string.error_server) -> {
                DMELog.e("An error is received from a server call.")
                error = AuthError.Server
            }
            else -> {
                DMELog.i("User accepted consent request.")
            }
        }

        authorizationCallbackHandler?.invoke(
            ConsentAuthResponse(
                success.toBoolean(),
                code,
                state,
                postboxId,
                publicKey
            ), error
        )
        onboardingCallbackHandler?.invoke(error)
        AppCommunicator.getSharedInstance().removeCallbackHandler(this)
        authorizationCallbackHandler = null
        onboardingCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        // TODO: Does quark do metadata?
    }

    private fun buildSaaSClientURI(serviceId: String? = null, codeValue: String): Uri {

        val ctx = AppCommunicator.getSharedInstance().context

        val code = ctx.getString(R.string.saas_client_code)

        val uri: Uri.Builder = Uri.parse("${baseURL}apps/saas/$type")
            .buildUpon()
            .appendQueryParameter(code, codeValue)
            .appendQueryParameter("callback", "digime-ca://callback")

        serviceId?.let { uri.appendQueryParameter("service", serviceId) }

        return uri.build()
    }
}