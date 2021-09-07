package me.digi.sdk.interapp

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import me.digi.sdk.R
import me.digi.sdk.SDKError

class AppCommunicator(val context: Context) {

    companion object {

        // Mem leak warning not an issue as we enforce the context be app context. This is always held in mem anyway.
        private lateinit var _sharedInstance: AppCommunicator

        @JvmStatic
        fun getSharedInstance(): AppCommunicator {
            try {
                return _sharedInstance
            } catch (error: Throwable) {
                throw SDKError.CommunicatorNotInitialized()
            }
        }

        /**
         * If your application is working with 'startActivityForResult' method,
         * use this override
         */
        fun getSharedInstance(context: Context): AppCommunicator =
            if (::_sharedInstance.isInitialized) _sharedInstance
            else initializeSharedInstance(context)

        fun initializeSharedInstance(context: Context): AppCommunicator {
            _sharedInstance = AppCommunicator(context)
            return _sharedInstance
        }
    }

    private var callbackHandlers = emptyList<AppCallbackHandler>().toMutableList()

    fun buildActionFor(@StringRes deeplinkResId: Int): String {
        val intentPrefix = "android.intent.action.me.digi."
        val deeplinkSuffix = context.getString(deeplinkResId)
        return intentPrefix + deeplinkSuffix
    }

    fun onActivityResult(requestCode: Int, responseCode: Int, data: Intent?) {
        val availableHandlers =
            callbackHandlers.filter { it.canHandle(requestCode, responseCode, data) }
        availableHandlers.forEach {
            it.handle(data)
        }
    }

    fun addCallbackHandler(handler: AppCallbackHandler) {
        if (!callbackHandlers.contains(handler)) {
            callbackHandlers.add(handler)
        }
    }

    fun removeCallbackHandler(handler: AppCallbackHandler) {
        if (callbackHandlers.contains(handler)) {
            callbackHandlers.remove(handler)
        }
    }

    fun requestCodeForDeeplinkIntentAction(deeplinkIntentAction: String) =
        when (deeplinkIntentAction) {
            buildActionFor(R.string.deeplink_consent_access) -> 18450
            buildActionFor(R.string.deeplink_create_postbox) -> 18451
            buildActionFor(R.string.deeplink_guest_consent_callback) -> 18452
            else -> 0
        }

    fun requestCodeForDeeplinkIntentActionId(@StringRes deeplinkIntentActionId: Int) =
        requestCodeForDeeplinkIntentAction(buildActionFor(deeplinkIntentActionId))

}