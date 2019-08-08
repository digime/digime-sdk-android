package me.digi.sdk.interapp.managers

import android.content.Context
import android.content.Intent
import me.digi.sdk.R
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator

class DMENativeConsentManager(appCommunicator: DMEAppCommunicator): DMEAppCallbackHandler(appCommunicator) {

    fun beginAuthorization(completion: DMEAuthorizationCompletion) {

    }

    override fun canHandle(intent: Intent) = when (intent.action) {
        appCommunicator.buildActionFor(R.string.deeplink_consent_access) -> {

            true
        }
        else -> false
    }

    override fun handle(intent: Intent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}