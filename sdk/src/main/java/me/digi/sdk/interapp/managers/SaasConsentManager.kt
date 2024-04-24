package me.digi.sdk.interapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import me.digi.sdk.AuthError
import me.digi.sdk.R
import me.digi.sdk.entities.response.SaasCallbackResponse
import me.digi.sdk.interapp.*
import me.digi.sdk.ui.ConsentBrowserActivity
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.toMap

class SaasConsentManager(private val baseURL: String?, private val type: String) :
    AppCallbackHandler() {

    private var authorizationCallbackHandler: AuthorizationCallback? = null
        set(value) {
            if (field != null && value != null)
                field?.invoke(null, AuthError.Cancelled)

            field = value
        }

    private var onboardingCallbackHandler: ServiceOnboardCallback? = null
        set(value) {
            if (field != null && value != null)
                field?.invoke(null, AuthError.Cancelled)

            field = value
        }

    private var reAuthCallbackHandler: ServiceReAuthCallback? = null
        set(value) {
            if (field != null && value != null)
                field?.invoke(null, AuthError.Cancelled)

            field = value
        }

    fun beginConsentAction(
        fromActivity: Activity,
        codeValue: String,
        serviceId: String? = null,
        sampleData: Boolean?,
        callback: AuthorizationCallback,
        ) {
        AppCommunicator.getSharedInstance().addCallbackHandler(this)
        authorizationCallbackHandler = callback
        handlerAction(fromActivity, "service", serviceId, codeValue, sampleData)
    }

    fun beginOnboardAction(
        fromActivity: Activity,
        codeValue: String,
        serviceId: String? = null,
        sampleData: Boolean?,
        callback: ServiceOnboardCallback
        ) {
        AppCommunicator.getSharedInstance().addCallbackHandler(this)
        onboardingCallbackHandler = callback
        handlerAction(fromActivity, "service", serviceId, codeValue, sampleData)
    }
    fun beginReAuthAction(
        fromActivity: Activity,
        codeValue: String,
        accountId: String? = null,
        sampleData: Boolean?,
        callback: ServiceReAuthCallback
        ) {
        AppCommunicator.getSharedInstance().addCallbackHandler(this)
        reAuthCallbackHandler = callback
        handlerAction(fromActivity, "accountRef", accountId, codeValue, sampleData)
    }

    private fun handlerAction(fromActivity: Activity, queryParameterKey: String, queryParameter: String?, codeValue: String, sampleData: Boolean?) {
        val guestRequestCode = AppCommunicator.getSharedInstance()
            .requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)

        val proxyLaunchIntent = Intent(fromActivity, ConsentBrowserActivity::class.java)

        proxyLaunchIntent.data = buildSaaSClientURI(queryParameterKey, queryParameter, codeValue, sampleData)

        fromActivity.startActivityForResult(proxyLaunchIntent, guestRequestCode)
    }

    private fun revokeHandlerAction(fromActivity: Activity, revokeUrl: String) {
        val guestRequestCode = AppCommunicator.getSharedInstance()
            .requestCodeForDeeplinkIntentActionId(R.string.deeplink_guest_consent_callback)

        val proxyLaunchIntent = Intent(fromActivity, ConsentBrowserActivity::class.java)

        proxyLaunchIntent.data = Uri.parse(revokeUrl)

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
            SaasCallbackResponse(
                success.toBoolean(),
                code,
                state,
                postboxId,
                publicKey
            ), error
        )
        onboardingCallbackHandler?.invoke(
            SaasCallbackResponse(
                success.toBoolean(),
                code,
                state,
                postboxId,
                publicKey
            ),
            error)

        reAuthCallbackHandler?.invoke(
            SaasCallbackResponse(
                success.toBoolean(),
                code,
                state,
                postboxId,
                publicKey
            ),
            error)

        AppCommunicator.getSharedInstance().removeCallbackHandler(this)
        authorizationCallbackHandler = null
        onboardingCallbackHandler = null
        reAuthCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        // TODO: Does quark do metadata?
    }

    private fun buildSaaSClientURI(queryParameterKey: String, queryParameter: String? = null, codeValue: String, sampleData: Boolean?): Uri {

        val ctx = AppCommunicator.getSharedInstance().context

        val code = ctx.getString(R.string.saas_client_code)

        val uri: Uri.Builder = Uri.parse("${baseURL}apps/saas/$type")
            .buildUpon()
            .appendQueryParameter(code, codeValue)

        queryParameter?.let { uri.appendQueryParameter(queryParameterKey, queryParameter) }

        sampleData?.let {
            uri.appendQueryParameter("sampleDataAutoOnboard", sampleData.toString())
            uri.appendQueryParameter("sampleDataSet", "default")
            uri.appendQueryParameter("includeSampleDataOnlySources", true.toString())
        }

        return uri.build()
    }
}