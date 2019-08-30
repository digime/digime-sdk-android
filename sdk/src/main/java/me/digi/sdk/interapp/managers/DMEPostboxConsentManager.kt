package me.digi.sdk.interapp.managers

import android.content.Intent
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator

class DMEPostboxConsentManager: DMEAppCallbackHandler() {

    fun beginPostboxAuthorization(completion: DMEAuthorizationCompletion) {

    }

    override fun canHandle(requestCode: Int, responseCode: Int, data:Intent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handle(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}