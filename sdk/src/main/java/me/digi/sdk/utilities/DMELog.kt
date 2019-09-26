package me.digi.sdk.utilities

import android.util.Log

internal object DMELog {

    var debugLogEnabled = false
    private const val dmeTag = "DigiMeSDK"

    fun d(message: String) {
        if (debugLogEnabled)
            Log.d(dmeTag, message)
    }

    fun i(message: String) {
        Log.i(dmeTag, message)
    }

    fun e(message: String) {
        Log.e(dmeTag, message)
    }
}