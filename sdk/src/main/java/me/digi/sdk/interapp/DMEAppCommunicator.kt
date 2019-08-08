package me.digi.sdk.interapp

import android.content.Context
import android.content.Intent
import android.support.annotation.StringRes
import me.digi.sdk.R

class DMEAppCommunicator(val context: Context) {

    fun canOpenDMEApp(): Boolean {
        val packageManager = context.packageManager
        val digiMeAppPackageName = context.getString(R.string.const_digime_app_package_name)

        return try {
            packageManager.getApplicationInfo(digiMeAppPackageName, 0).enabled
        }
        catch (error: Throwable) {
            return false
        }
    }

    fun buildActionFor(@StringRes deeplinkResId: Int): String {
        val intentPrefix = "com.android.intent.action.me.digi."
        val deeplinkSuffix = context.getString(deeplinkResId)
        return intentPrefix + deeplinkSuffix
    }

    fun buildIntentFor(@StringRes deeplinkResId: Int, params: Map<String, String>): Intent {
        val action = buildActionFor(deeplinkResId)
        val intent = Intent(action)
        params.forEach(intent::putExtra)

        return intent
    }
}