package me.digi.sdk.interapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.StringRes
import me.digi.sdk.DMESDKError
import me.digi.sdk.R

class DMEAppCommunicator(val context: Context) {

    companion object {

        // Mem leak warning not an issue as we enforce the context be app context. This is always held in mem anyway.
        private lateinit var _sharedInstance: DMEAppCommunicator

        fun getSharedInstance(): DMEAppCommunicator {
            try {
                return _sharedInstance
            } catch(error: Throwable) {
                throw DMESDKError.CommunicatorNotInitialized()
            }
        }

        fun initializeSharedInstance(context: Context): DMEAppCommunicator {
            _sharedInstance = DMEAppCommunicator(context)
            return _sharedInstance
        }
    }

    private var callbackHandlers = emptyList<DMEAppCallbackHandler>().toMutableList()

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
        val action = /*buildActionFor(deeplinkResId)*/ "android.intent.action.DIGI_PERMISSION_REQUEST"
        val intent = Intent()
        intent.setAction(action)
        intent.setPackage(context.getString(R.string.const_digime_app_package_name))
        intent.setType("text/plain")
        params.forEach { intent.putExtra(it.key, it.value) }

        return intent
    }

    fun openDigiMeApp(fromActivity: Activity, intent: Intent) {
        fromActivity.startActivityForResult(intent, 762)
    }

    fun onActivityResult(requestCode: Int, responseCode: Int, data: Intent?) {

        if (data != null) {
            val availableHandlers = callbackHandlers.filter { it.canHandle(data) }
            availableHandlers.forEach {
                it.handle(data)
            }
        }
    }

    fun addCallbackHandler(handler: DMEAppCallbackHandler) {
        if (!callbackHandlers.contains(handler)) {
            callbackHandlers.add(handler)
        }
    }

    fun removeCallbackHandler(handler: DMEAppCallbackHandler) {
        if (callbackHandlers.contains(handler)) {
            callbackHandlers.remove(handler)
        }
    }
}