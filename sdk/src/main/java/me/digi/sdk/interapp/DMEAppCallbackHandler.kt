package me.digi.sdk.interapp

import android.content.Intent
import android.os.Bundle

abstract class DMEAppCallbackHandler {

    abstract fun canHandle(intent: Intent): Boolean

    abstract fun handle(intent: Intent)

}