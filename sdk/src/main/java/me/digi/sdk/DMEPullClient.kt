package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.digi.sdk.callbacks.*
import me.digi.sdk.callbacks.DMEFileListCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.interapp.managers.DMEGuestConsentManager
import me.digi.sdk.interapp.managers.DMENativeConsentManager
import me.digi.sdk.utilities.DMEFileListItemCache
import me.digi.sdk.utilities.DMELog

class DMEPullClient(val context: Context, val configuration: DMEPullConfiguration): DMEClient(context, configuration) {

    private val nativeConsentManager: DMENativeConsentManager by lazy { DMENativeConsentManager(sessionManager, configuration.appId) }
    private val guestConsentManager: DMEGuestConsentManager by lazy { DMEGuestConsentManager(sessionManager, configuration.baseUrl) }

    private var activeFileDownloadHandler: DMEFileContentCompletion? = null
    private var activeSessionDataFetchCompletionHandler: DMEEmptyCompletion? = null
    private var fileListItemCache: DMEFileListItemCache? = null
    private var activeSyncState: DMEFileList.SyncState? = null
        set(value) {
            val previousValue = field
            if (previousValue != value && previousValue != null && value != null)
                DMELog.d("Sync state changed. Previous: ${previousValue.rawValue}. New: ${value.rawValue}.")

            if (activeDownloadCount == 0) {
                when (value) {
                    DMEFileList.SyncState.COMPLETED() -> completeDeliveryOfSessionData(null)
                    DMEFileList.SyncState.PARTIAL() -> completeDeliveryOfSessionData(DMEAPIError.PartialSync())
                    else -> Unit
                }
            }

            field = value
        }
    private var activeDownloadCount = 0
        set(value) {
            field = value
            if (value == 0) {
                when (activeSyncState) {
                    DMEFileList.SyncState.COMPLETED() -> completeDeliveryOfSessionData(null)
                    DMEFileList.SyncState.PARTIAL() -> completeDeliveryOfSessionData(DMEAPIError.PartialSync())
                    else -> Unit
                }
            }
        }

    fun authorize(fromActivity: Activity, completion: DMEAuthorizationCompletion) = authorize(fromActivity, null, completion)

    fun authorize(fromActivity: Activity, scope: DMEDataRequest?, completion: DMEAuthorizationCompletion) {

        DMELog.i("Launching user consent request.")

        val req = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", scope)
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                when (Pair(DMEAppCommunicator.getSharedInstance().canOpenDMEApp(), configuration.guestEnabled)) {
                    Pair(true, true),
                    Pair(true, false) -> nativeConsentManager.beginAuthorization(fromActivity, completion)
                    Pair(false, true) -> guestConsentManager.beginGuestAuthorization(fromActivity, completion)
                    Pair(false, false) -> completion(null, DMESDKError.DigiMeAppNotFound())
                }
            }
            else {
                DMELog.e("An error occurred whilst communicating with our servers: ${error?.message}")
                completion(null, error)
            }
        }
    }

    fun getSessionData(downloadHandler: DMEFileContentCompletion, completion: DMEEmptyCompletion) {

        DMELog.i("Starting fetch of session data.")

        // Init state.
        fileListItemCache = DMEFileListItemCache()
        activeFileDownloadHandler = downloadHandler
        activeSessionDataFetchCompletionHandler = completion

        scheduleNextPoll(true)
    }

    fun getSessionData(fileId: String, completion: DMEFileContentCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            apiClient.makeCall(apiClient.argonService.getFile(currentSession.key, fileId), completion)

        }
        else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }

    }

    internal fun getFileList(completion: DMEFileListCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {
            apiClient.makeCall(apiClient.argonService.getFileList(currentSession.key), completion)
        }
        else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    fun getSessionAccounts(completion: DMEAccountsCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            DMELog.i("Starting account fetch.")

            apiClient.makeCall(apiClient.argonService.getFile(currentSession.key, "accounts.json")) { file, error ->

                if (file == null) {
                    DMELog.e("Failed to fetch accounts. Error: ${error?.message}")
                    completion(null, error)
                }

                val accountsFileJSON = file?.fileContentAsJSON()
                val accountsType = object: TypeToken<List<DMEAccount>>(){}.type

                val accounts = try { Gson().fromJson<List<DMEAccount>>(accountsFileJSON, accountsType) } catch( e: Throwable) { emptyList<DMEAccount>() }
                DMELog.i("Successfully fetched accounts: ${accounts.map { it.identifier }}")
                completion(accounts, error)
            }

        }
        else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }

    }

    private fun scheduleNextPoll(immediately: Boolean = false) {

        DMELog.d("Session data poll scheduled.")

        val delay = (if (immediately) 0 else 3000).toLong()
        Handler().postDelayed({

            DMELog.d("Fetching file list.")
            getFileList { fileList, listFetchError ->

                when {
                    fileList != null -> DMELog.d("File list obtained; Sync state is ${fileList.state.rawValue}.")
                    listFetchError != null -> DMELog.d("Error fetching file list: ${listFetchError.message}.")
                }

                val syncState = fileList?.state ?: DMEFileList.SyncState.RUNNING()

                val updatedFileIds = fileListItemCache?.updateCacheWithItemsAndDeduceChanges(fileList?.fileList.orEmpty()).orEmpty()
                DMELog.i("${fileList?.fileList.orEmpty().count()} files discovered. Of these, ${updatedFileIds.count()} have updates and need downloading.")
                if (updatedFileIds.count() > 0)
                    scheduledPollDidDiscoverUpdatedFiles(updatedFileIds)

                when (syncState) {
                    DMEFileList.SyncState.PENDING(),
                    DMEFileList.SyncState.RUNNING() -> scheduleNextPoll()
                    else -> Unit
                }

                activeSyncState = syncState
            }

        }, delay)
    }

    private fun scheduledPollDidDiscoverUpdatedFiles(updatedFileIds: List<String>) {
        // We have updated files, attempt to fetch them.

        DMELog.i("Discovered files with updates.")

        updatedFileIds.forEach {

            activeDownloadCount++
            DMELog.d("Downloading file with ID: $it.")

            getSessionData(it) { file, error ->

                when {
                    file != null -> DMELog.i("Successfully downloaded updates for file with ID: $it.")
                    else -> DMELog.e("Failed to download updates for file with ID: $it.")
                }

                activeDownloadCount--
                activeFileDownloadHandler?.invoke(file, error)
            }
        }
    }

    private fun completeDeliveryOfSessionData(error: DMEError?) {

        when {
            error != null -> DMELog.e("An error occurred whilst fetching session data. Error: ${error.message}")
            else -> DMELog.i("Session data fetch completed successfully.")
        }

        activeSessionDataFetchCompletionHandler?.invoke(error)

        // Clear state.
        fileListItemCache = null
        activeFileDownloadHandler = null
        activeSessionDataFetchCompletionHandler = null
        activeSyncState = null
        activeDownloadCount = 0
    }

}