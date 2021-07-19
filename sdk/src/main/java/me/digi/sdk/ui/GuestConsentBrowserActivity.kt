package me.digi.sdk.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import me.digi.sdk.R

class GuestConsentBrowserActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guest_consent_browser_activity)

        val intentUri = intent?.data
            ?: throw IllegalStateException("GuestConsentBrowserActivity must be started with an intent.")

        if (intentUri.scheme.orEmpty() == getString(R.string.deeplink_guest_consent_callback)) {
            handleWebOnboardingCallback(intentUri)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, intentUri))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val intentUri = intent?.data
            ?: throw IllegalStateException("GuestConsentBrowserActivity must be started with an intent.")
        handleWebOnboardingCallback(intentUri)
    }

    private fun handleSaasCallback(intentUri: Uri) {
        val state = intentUri.getQueryParameter(getString(R.string.key_state))
        val code = intentUri.getQueryParameter(getString(R.string.key_code))
        val postboxId = intentUri.getQueryParameter(getString(R.string.key_s_postbox_id))
        val publicKey = intentUri.getQueryParameter(getString(R.string.key_s_public_key))
        val success: String? = intentUri.getQueryParameter(getString(R.string.key_success))
        val error = intentUri.getQueryParameter(getString(R.string.key_error))

        if(success.toBoolean()) {
            intent?.putExtra(getString(R.string.key_code), code)
            intent?.putExtra(getString(R.string.key_state), state)
            intent?.putExtra(getString(R.string.key_s_postbox_id), postboxId)
            intent?.putExtra(getString(R.string.key_s_public_key), publicKey)
            setResult(RESULT_OK, intent)
        } else {
            intent?.putExtra(getString(R.string.key_error), error)
            setResult(RESULT_CANCELED, intent)
        }
    }

    private fun handleWebOnboardingCallback(intentUri: Uri) {
        handleSaasCallback(intentUri)
        finish()
    }
}