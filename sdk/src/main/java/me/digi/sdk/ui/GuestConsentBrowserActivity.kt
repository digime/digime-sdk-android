package me.digi.sdk.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import me.digi.sdk.R

class GuestConsentBrowserActivity: Activity() {

    companion object {
        val QUARK_LAUNCH_URL_EXTRA_KEY = "launchURL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guest_consent_browser_activity)

        val intentUri = intent?.data ?: throw IllegalStateException("GuestConsentBrowserActivity must be started with an intent.")

        if (intentUri.scheme.orEmpty() == getString(R.string.deeplink_guest_consent_callback)) {
            // Launched by Quark returning from onboarding.
            handleWebOnboardingCallback(intentUri)
        }
        else {
            // Launched by DMEGuestConsentManager.
            startActivity(Intent(Intent.ACTION_VIEW, intentUri))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val intentUri = intent?.data ?: throw IllegalStateException("GuestConsentBrowserActivity must be started with an intent.")
        handleWebOnboardingCallback(intentUri)
    }

    private fun handleWebOnboardingCallback(intentUri: Uri) {
        val result = intentUri.getQueryParameter(getString(R.string.key_result))
        if (result != null && result == getString(R.string.const_result_data_ready)) {
            intent?.putExtra(getString(R.string.key_result), getString(R.string.const_result_success))
            setResult(RESULT_OK)
        }
        else {
            intent?.putExtra(getString(R.string.key_result), getString(R.string.const_result_cancel))
            setResult(RESULT_CANCELED)
        }
        finish()
    }
}