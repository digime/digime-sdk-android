package me.digi.sdk

import android.app.Activity
import android.content.Context
import me.digi.sdk.api.helpers.DMEMultipartBody
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.callbacks.DMEPostboxPushCompletion
import me.digi.sdk.entities.DMEPushConfiguration
import me.digi.sdk.entities.DMEPushPayload
import me.digi.sdk.entities.DMESDKAgent
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.managers.DMEPostboxConsentManager
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEDataEncryptor

class DMEPushClient(val context: Context, val configuration: DMEPushConfiguration) :
    DMEClient(context, configuration) {

    private val postboxConsentManager: DMEPostboxConsentManager by lazy {
        DMEPostboxConsentManager(
            sessionManager,
            configuration.appId
        )
    }

    fun createPostbox(fromActivity: Activity, completion: DMEPostboxCreationCompletion) {

        DMELog.i("Launching user consent request.")

        val req = DMESessionRequest(
            configuration.appId,
            configuration.contractId,
            DMESDKAgent(),
            "gzip",
            null
        )
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                postboxConsentManager.beginPostboxAuthorization(fromActivity, completion)
            } else {
                DMELog.e("An error occurred whilst communicating with our servers: ${error?.message}")
                completion(null, error)
            }
        }
    }

    fun pushDataToPostbox(postboxFile: DMEPushPayload, completion: DMEPostboxPushCompletion) {
        DMELog.i("Initializing push data to postbox.")

        if (sessionManager.isSessionValid()) {
            val encryptedData = DMEDataEncryptor.encryptedDataFromBytes(
                postboxFile.dmePostbox.publicKey,
                postboxFile.content,
                postboxFile.metadata
            )

            val multipartBody = DMEMultipartBody.Builder()
                .postboxPushPayload(postboxFile)
                .dataContent(encryptedData.fileContent, postboxFile.mimeType)
                .build()

            apiClient.makeCall(
                apiClient.argonService.pushData(
                    postboxFile.dmePostbox.sessionKey,
                    encryptedData.symmetricalKey,
                    encryptedData.iv,
                    encryptedData.metadata,
                    postboxFile.dmePostbox.postboxId,
                    multipartBody.requestBody,
                    multipartBody.description
                )
            ) { _, error ->

                if (error != null) {
                    DMELog.e("Failed to push file to postbox. Error: ${error.message}")
                    completion(error)
                }

                DMELog.i("Successfully pushed data to postbox")
                completion(null)
            }
        }
    }
}

