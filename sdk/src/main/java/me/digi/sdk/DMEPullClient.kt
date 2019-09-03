package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import me.digi.sdk.callbacks.DMEAccountsCompletion
import me.digi.sdk.callbacks.DMEAuthorizationCompletion
import me.digi.sdk.callbacks.DMEFileContentCompletion
import me.digi.sdk.callbacks.DMEFileListCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.managers.DMENativeConsentManager

class DMEPullClient(val context: Context, val configuration: DMEClientConfiguration): DMEClient(context, configuration) {

    private val nativeConsentManager: DMENativeConsentManager by lazy { DMENativeConsentManager(sessionManager, configuration.appId) }

    fun authorize(fromActivity: Activity, completion: DMEAuthorizationCompletion) = authorize(fromActivity, null, completion)

    fun authorize(fromActivity: Activity, scope: DMEDataRequest?, completion: DMEAuthorizationCompletion) {
        val req = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", scope)
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                nativeConsentManager.beginAuthorization(fromActivity, completion)
            }
            else {
                completion(null, error)
            }
        }
    }

    fun getSessionData(downloadHandler: DMEFileContentCompletion, completion: (DMEError?) -> Unit) {

    }

    fun getSessionData(fileId: String, completion: DMEFileContentCompletion) {

    }

    fun getFileList(completion: DMEFileListCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {
            apiClient.makeCall(apiClient.argonService.getFileList(currentSession.key)) { envelope, error ->

                completion(envelope?.fileIds, error)

            }
        }
        else {
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    fun getSessionAccounts(completion: DMEAccountsCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            apiClient.makeCall(apiClient.argonService.getFile(currentSession.key, "accounts.json")) { file, error ->

                if (file == null) {
                    completion(null, error)
                }

                val accountsFileMap = file?.fileContentAsJSON().orEmpty()
                val accountsType = object: TypeToken<List<DMEAccount>>(){}.type
                val accountsFileJSON = Gson().toJsonTree(accountsFileMap)

                val accounts = try { Gson().fromJson<List<DMEAccount>>(accountsFileJSON, accountsType) } catch( e: Throwable) { emptyList<DMEAccount>() }
                completion(accounts, error)
            }

        }
        else {
            completion(null, DMEAuthError.InvalidSession())
        }

    }

}