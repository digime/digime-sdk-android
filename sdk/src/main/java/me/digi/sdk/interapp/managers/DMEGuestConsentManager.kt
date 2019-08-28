package me.digi.sdk.interapp.managers

import android.content.Intent
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator

class DMEGuestConsentManager: DMEAppCallbackHandler() {

    fun beginGuestAuthorization(completion: DMEAuthorizationCompletion) {
        
    }

    override fun canHandle(intent: Intent): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handle(intent: Intent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}