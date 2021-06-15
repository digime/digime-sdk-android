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

    private fun handleSaaSAuthorizeCallback(intentUri: Uri) {
        val state = intentUri.getQueryParameter("state")
        val code = intentUri.getQueryParameter("code")

        if (code != null && state != null) {
            intent?.putExtra(
                getString(R.string.key_result),
                getString(R.string.const_result_success)
            )
            intent?.putExtra("code", code)
            intent?.putExtra("state", state)
            setResult(RESULT_OK, intent)
        } else {
            intent?.putExtra(
                getString(R.string.key_result),
                getString(R.string.const_result_cancel)
            )
            setResult(RESULT_CANCELED, intent)
        }
    }

    private fun handleSaaSOnboardingCallback(intentUri: Uri) {
        if (intentUri.host.equals("onboarding-success")) {
            intent?.putExtra(
                getString(R.string.key_result),
                getString(R.string.const_result_success)
            )
            setResult(RESULT_OK, intent)
        } else {
            intent?.putExtra(
                getString(R.string.key_result),
                getString(R.string.const_result_cancel)
            )
            setResult(RESULT_CANCELED, intent)
        }
    }

    private fun handleWebOnboardingCallback(intentUri: Uri) {

        if (intentUri.host?.contains("onboarding")!!) {
            handleSaaSOnboardingCallback(intentUri)
        } else {
            handleSaaSAuthorizeCallback(intentUri)
        }

        finish()
    }
}