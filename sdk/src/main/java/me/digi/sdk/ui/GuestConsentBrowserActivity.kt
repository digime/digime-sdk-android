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
        val state = intentUri.getQueryParameter("state")
        val code = intentUri.getQueryParameter("code")
        val postboxId = intentUri.getQueryParameter("postboxId")
        val publicKey = intentUri.getQueryParameter("publicKey")
        val success = intentUri.getQueryParameter("success")
        val error = intentUri.getQueryParameter("errorCode")

        success?.let {
            if(it == "true") {
                intent?.putExtra(
                    getString(R.string.key_result),
                    getString(R.string.const_result_success)
                )
                intent?.putExtra("success", success)
                intent?.putExtra("error", error)
                intent?.putExtra("code", code)
                intent?.putExtra("state", state)
                intent?.putExtra("postboxId", postboxId)
                intent?.putExtra("publicKey", publicKey)
                setResult(RESULT_OK, intent)
            } else {
                intent?.putExtra(
                    getString(R.string.key_result),
                    getString(R.string.const_result_cancel)
                )
                setResult(RESULT_CANCELED, intent)
            }
        }
    }

    private fun handleWebOnboardingCallback(intentUri: Uri) {
        handleSaasCallback(intentUri)
        finish()
    }
}