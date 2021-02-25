package me.digi.sdk.interapp.managers

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import me.digi.sdk.DMEAuthError
import me.digi.sdk.R
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.callbacks.DMEPostboxAuthCompletion
import me.digi.sdk.entities.DMEPostbox
import me.digi.sdk.interapp.DMEAppCallbackHandler
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.*

class DMEOngoingPostboxConsentManager(
    private val sessionManager: DMESessionManager,
    val appId: String
) : DMEAppCallbackHandler() {

    private var pendingOngoingPostboxCallbackHandler: DMEPostboxAuthCompletion? = null
        set(value) {
            if (field != null && value != null)
                field?.invoke(null, null, DMEAuthError.Cancelled())

            field = value
        }

    fun beginOngoingPostboxAuthorization(
        fromActivity: Activity,
        completion: DMEPostboxAuthCompletion
    ) {

        DMEAppCommunicator.getSharedInstance().addCallbackHandler(this)

        val context = DMEAppCommunicator.getSharedInstance().context

        sessionManager.currentSession?.let { session ->

            val caParams = mutableMapOf(
                context.getString(R.string.key_session_key) to session.key,
                context.getString(R.string.key_app_id) to appId
            )

            if (sessionManager.currentSession?.preauthorizationCode != null)
                caParams[context.getString(R.string.key_preauthorization_code)] =
                    sessionManager.currentSession!!.preauthorizationCode!!

            val launchIntent = DMEAppCommunicator.getSharedInstance()
                .buildIntentFor(R.string.deeplink_create_postbox, caParams)
            pendingOngoingPostboxCallbackHandler = completion
            DMEAppCommunicator.getSharedInstance().openDigiMeApp(fromActivity, launchIntent)

            // For Android 10 and above, backgrounded tasks can no longer start
            // activities. As such, we can't trigger CA led onboarding when digi.me
            // installation finishes. As a workaround (sort of), we will pop a
            // notification inviting the user to tap it. This will wake the app
            // and allow the flow to resume.
            if (Build.VERSION.SDK_INT >= 29) {

                // Configure manager.
                val notificationManager =
                    fromActivity.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                // Configure channel.
                val notificationChannelId =
                    fromActivity.getString(R.string.const_android10_notification_channel_id)
                val notificationChannelName =
                    fromActivity.getString(R.string.const_android10_notification_channel_name)
                val notificationChannel = NotificationChannel(
                    notificationChannelId,
                    notificationChannelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager?.createNotificationChannel(notificationChannel)

                // Configure notification.
                val relaunchIntent = Intent(fromActivity, DMEResumeStateActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    fromActivity,
                    0,
                    relaunchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val hostAppName = fromActivity.getString(fromActivity.applicationInfo.labelRes)
                val notificationTitle =
                    fromActivity.getString(R.string.const_android10_notification_title)
                val notificationBody =
                    fromActivity.getString(R.string.const_android10_notification_body)
                val notificationAction = fromActivity.getString(
                    R.string.const_android10_notification_action,
                    hostAppName.toUpperCase()
                )
                val notificationIconDrawable =
                    fromActivity.applicationInfo.loadIcon(fromActivity.packageManager)
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
            completion(null, null, DMEAuthError.InvalidSession())
        }
    }

    override fun canHandle(requestCode: Int, responseCode: Int, data: Intent?): Boolean {
        val communicator = DMEAppCommunicator.getSharedInstance()
        return (requestCode == communicator.requestCodeForDeeplinkIntentActionId(R.string.deeplink_create_postbox))
    }

    override fun handle(intent: Intent?) {

        val context = DMEAppCommunicator.getSharedInstance().context
        val params = intent?.extras?.toMap() ?: emptyMap()

        val (errorCode, errorMessage, errorReference) = Triple(
            params["argonErrorCode"] as? String,
            params["argonErrorMessage"] as? String,
            params["argonErrorReference"] as? String
        )

        if (intent == null) {
            // Received no data, Android system failed to start activity.
            DMELog.e("There was a problem launching the consent request activity.")
            pendingOngoingPostboxCallbackHandler?.invoke(sessionManager.currentSession, null, DMEAuthError.General())
            DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
            pendingOngoingPostboxCallbackHandler = null
            return
        }

        val result = params[context.getString(R.string.key_result)] as? String

        val postboxId = params[context.getString(R.string.key_postbox_id)] as? String
        val postboxPublicKey = params[context.getString(R.string.key_public_key)] as? String
        val sessionKey = params[context.getString(R.string.key_session_key)] as? String
        val digiMeVersion = params[context.getString(R.string.key_app_version)] as? String

        val authorizationCode = params[context.getString(R.string.key_authorization_code)] as? String
        sessionManager.currentSession?.authorizationCode = authorizationCode

        DMELog.i("""
            DATA:
            1. Result: $result
            2. PostboxID: $postboxId
            3. Postbox PK: $postboxPublicKey
            4. Session key: $sessionKey
            5. Digime v: $digiMeVersion
            6. AuthCode: $authorizationCode
            7. Session auth code: ${sessionManager.currentSession?.authorizationCode}
        """.trimIndent())

        extractAndAppendMetadata(params)

        val error = if (!sessionManager.isSessionValid()) {
            DMEAuthError.InvalidSession()
        } else when (result) {
            context.getString(R.string.const_result_error) -> {
                DMELog.e("There was a problem requesting consent.")
                DMEAPIClient.parseDMEError(errorCode, errorMessage, errorReference)
            }
            context.getString(R.string.const_result_cancel) -> {
                DMELog.e("User rejected consent request.")
                DMEAuthError.Cancelled()
            }
            else -> {
                DMELog.i("User accepted consent request.")
                null
            }
        }

        val postbox = DMEPostbox(sessionKey!!, postboxId!!, postboxPublicKey!!, digiMeVersion)
        pendingOngoingPostboxCallbackHandler?.invoke(sessionManager.currentSession, postbox, error)
        DMEAppCommunicator.getSharedInstance().removeCallbackHandler(this)
        pendingOngoingPostboxCallbackHandler = null
    }

    override fun extractAndAppendMetadata(payload: Map<String, Any>) =
        sessionManager.currentSession?.let { session ->

            val ctx = DMEAppCommunicator.getSharedInstance().context

            val metadataWhitelistedKeys = listOf(

                // Functional Keys
                R.string.key_postbox_id,
                R.string.key_public_key,
                R.string.key_result,
                R.string.key_app_id,
                R.string.key_app_version

            ).map { ctx.getString(it) }

            session.metadata.putAll(payload.filter {
                metadataWhitelistedKeys.contains(it.key)
            })

        } ?: run { Unit }
}