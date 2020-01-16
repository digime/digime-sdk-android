package me.digi.sdk.interapp.managers

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import me.digi.sdk.DMEAuthError
import me.digi.sdk.DMESDKError
import me.digi.sdk.R
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.*
import me.digi.sdk.utilities.DMEDrawableUtils
import me.digi.sdk.utilities.DMELog

class DMENativeConsentManager(val sessionManager: DMESessionManager, val appId: String): DMEAppCallbackHandler() {

    private var pendingAuthCallbackHandler: DMEAuthorizationCompletion? = null
        set(value) {
            if (field != null && value != null) {
                field?.invoke(null, DMEAuthError.Cancelled())
            }
            field = value
        }


    fun beginAuthorization(fromActivity: Activity, completion: DMEAuthorizationCompletion) {

        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)

        val ctx = DMEAppCommunicator.getSharedInstance().context

        sessionManager.currentSession?.let { session ->

            val caParams = mapOf(
                ctx.getString(R.string.key_session_key) to session.key,
                ctx.getString(R.string.key_app_id) to appId
            ).toMutableMap()

            if (sessionManager.currentSession?.preauthorizationCode != null) {
                caParams[ctx.getString(R.string.key_preauthorization_code)] = sessionManager.currentSession!!.preauthorizationCode!!
            }

            val launchIntent = DMEAppCommunicator.getSharedInstance().buildIntentFor(R.string.deeplink_consent_access, caParams)
            pendingAuthCallbackHandler = completion
            DMEAppCommunicator.getSharedInstance().openDigiMeApp(fromActivity, launchIntent)

            // For Android 10 and above, backgrounded tasks can no longer start
            // activities. As such, we can't trigger CA led onboarding when digi.me
            // installation finishes. As a workaround (sort of), we will pop a
            // notification inviting the user to tap it. This will wake the app
            // and allow the flow to resume.
            if (Build.VERSION.SDK_INT >= 29) {

                // Configure manager.
                val notificationManager = fromActivity.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                // Configure channel.
                val notificationChannelId = fromActivity.getString(R.string.const_android10_notification_channel_id)
                val notificationChannelName = fromActivity.getString(R.string.const_android10_notification_channel_name)
                val notificationChannel = NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_HIGH)
                notificationManager?.createNotificationChannel(notificationChannel)

                // Configure notification.
                val relaunchIntent = Intent(fromActivity, DMEResumeStateActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(fromActivity, 0, relaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val hostAppName = fromActivity.getString(fromActivity.applicationInfo.labelRes)
                val notificationTitle = fromActivity.getString(R.string.const_android10_notification_title)
                val notificationBody = fromActivity.getString(R.string.const_android10_notification_body)
                val notificationAction = fromActivity.getString(R.string.const_android10_notification_action, hostAppName.toUpperCase())
                val notificationIconDrawable = fromActivity.applicationInfo.loadIcon(fromActivity.packageManager)
                val notificationIconBitmap = DMEDrawableUtils.createBitmap(notificationIconDrawable)

                val notification = Notification.Builder(fromActivity, notificationChannelId)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(Icon.createWithBitmap(notificationIconBitmap))
                    .setLargeIcon(notificationIconBitmap)
                    .setContentIntent(pendingIntent)
                    .addAction(fromActivity.applicationInfo.icon, notificationAction, pendingIntent)
                    .build()

                // Show notification.
                notificationManager?.notify(0, notification)
            }

        } ?: run {
            DMELog.e("Your session is invalid, please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data:Intent?): Boolean {
        val communicator = DMEAppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_consent_access))
    }

    override fun handle(intent: Intent?) {

        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the consent request activity.")
            pendingAuthCallbackHandler?.invoke(sessionManager.currentSession, DMEAuthError.General())
            DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
            pendingAuthCallbackHandler = null
            return
        }

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val params = intent.extras?.toMap() ?: emptyMap()
        val result = params[ctx.getString(R.string.key_result)] as? String

        val authorizationCode = params[ctx.getString(R.string.key_authorization_code)] as? String
        sessionManager.currentSession?.authorizationCode = authorizationCode

        extractAndAppendMetadata(params)

        val error = if (!sessionManager.isSessionValid()) {
            DMEAuthError.InvalidSession()
        }
        else when (result) {
            ctx.getString(R.string.const_result_error) -> {
                DMELog.e("There was a problem requesting consent.")
                DMEAuthError.General()
            }
            ctx.getString(R.string.const_result_cancel) -> {
                DMELog.e("User rejected consent request.")
                DMEAuthError.Cancelled()
            }
            else -> {
                DMELog.i("User accepted consent request.")
                null
            }
        }

        pendingAuthCallbackHandler?.invoke(sessionManager.currentSession, error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        pendingAuthCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) = sessionManager.currentSession?.let { session ->

        val ctx = DMEAppCommunicator.getSharedInstance().context

        val metadataWhitelistedKeys = listOf(
            // Functional Keys
            R.string.key_session_key,
            R.string.key_result,
            R.string.key_app_id,

            // Timing Keys
            R.string.key_timing_get_all_files,
            R.string.key_timing_get_file,
            R.string.key_timing_fetch_contract_permission,
            R.string.key_timing_fetch_account,
            R.string.key_timing_fetch_file_list,
            R.string.key_timing_fetch_session_key,
            R.string.key_timing_data_request,
            R.string.key_timing_fetch_contract_details,
            R.string.key_timing_update_contract_permission,
            R.string.key_timing_total,

            // Debug Keys
            R.string.key_debug_app_id,
            R.string.key_debug_bundle_version,
            R.string.key_debug_platform,
            R.string.key_debug_contract_type,
            R.string.key_debug_device_id,
            R.string.key_debug_digime_version,
            R.string.key_debug_user_id,
            R.string.key_debug_library_id,
            R.string.key_debug_pcloud_type,
            R.string.key_contract_id,
            R.string.key_app_name
        ).map { ctx.getString(it) }

        session.metadata = payload.filter {
            metadataWhitelistedKeys.contains(it.key)
        }

    } ?: run { Unit }
}