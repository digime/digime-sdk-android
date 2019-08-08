package me.digi.sdk.interapp

import android.content.Intent

abstract class DMEAppCallbackHandler(val appCommunicator: DMEAppCommunicator) {

    abstract fun canHandle(intent: Intent): Boolean

    abstract fun handle(intent: Intent)

}