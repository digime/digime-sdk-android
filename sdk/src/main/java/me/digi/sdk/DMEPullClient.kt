package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
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
    private var fileListUpdateHandler: DMEIncrementalFileListUpdate? = null
    private var fileListCompletionHandler: DMEEmptyCompletion? = null
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
            if (value == 0) {
                when (activeSyncState) {
                    DMEFileList.SyncState.COMPLETED() -> completeDeliveryOfSessionData(null)
                    DMEFileList.SyncState.PARTIAL() -> completeDeliveryOfSessionData(DMEAPIError.PartialSync())
                    else -> Unit
                }
            }

            field = value
        }

    private var stalePollCount = 0

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
                    Pair(false, false) -> {
                        DMEAppCommunicator.getSharedInstance().requestInstallOfDMEApp(fromActivity) {
                            nativeConsentManager.beginAuthorization(fromActivity, completion)
                        }
                    }
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


        activeFileDownloadHandler = downloadHandler
        activeSessionDataFetchCompletionHandler = completion

        getSessionFileList({ fileList, updatedFileIds ->

            updatedFileIds.forEach {

                activeDownloadCount++
                DMELog.d("Downloading file with ID: $it.")

                getSessionData(it) { file, error ->

                    when {
                        file != null -> DMELog.i("Successfully downloaded updates for file with ID: $it.")
                        else -> DMELog.e("Failed to download updates for file with ID: $it.")
                    }

                    downloadHandler?.invoke(file, error)
                    activeDownloadCount--
                }
            }

        }) { error ->
            if (error != null) {
                completion(error) // We only want to push this if the error exists, else
                // it'll cause a premature loop exit.
            }
        }
    }

    fun getSessionData(fileId: String, completion: DMEFileContentCompletion) {

        val currentSession = sessionManager.currentSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            apiClient.makeCall(apiClient.argonService.getFile(currentSession.key, fileId)) { file, error ->
                file?.identifier = fileId
                completion(file, error)
            }

        }
        else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }

    }

    fun getSessionFileList(updateHandler: DMEIncrementalFileListUpdate, completion: DMEEmptyCompletion) {

        fileListUpdateHandler = updateHandler
        fileListCompletionHandler = {
            if (activeFileDownloadHandler == null && activeSessionDataFetchCompletionHandler == null) {
                completeDeliveryOfSessionData(it)
            }
            completion(it)
        }

        if (activeSyncState == null) {
            // Init state.
            fileListItemCache = DMEFileListItemCache()
            scheduleNextPoll(true)
        }
    }

    fun getFileList(completion: DMEFileListCompletion) {

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

                val accountsFileJSON = file?.fileContentAs(DMEAccountList::class.java)
                val accounts = accountsFileJSON?.accounts

                DMELog.i("Successfully fetched accounts: ${accounts?.map { it.id }}")
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

        val delay = (if (immediately) 0 else (configuration.pollingDelay * 1000)).toLong()
        Handler().postDelayed({

            DMELog.d("Fetching file list.")
            getFileList { fileList, listFetchError ->

                when {
                    fileList != null -> DMELog.d("File list obtained; Sync state is ${fileList.syncState.rawValue}.")
                    listFetchError != null -> DMELog.d("Error fetching file list: ${listFetchError.message}.")
                }

                var syncStatus = fileList?.syncState ?: DMEFileList.SyncState.RUNNING()

                val updatedFileIds = fileListItemCache?.updateCacheWithItemsAndDeduceChanges(fileList?.fileList.orEmpty()).orEmpty()
                DMELog.i("${fileList?.fileList.orEmpty().count()} files discovered. Of these, ${updatedFileIds.count()} have updates and need downloading.")

                if (updatedFileIds.count() > 0 && fileList != null) {
                    fileListUpdateHandler?.invoke(fileList, updatedFileIds)
                    stalePollCount = 0
                }
                else if (++stalePollCount == configuration.pollingRetryCount){
                    syncStatus = DMEFileList.SyncState.PARTIAL() // Force sync to end as partial.
                }

                when (syncStatus) {
                    DMEFileList.SyncState.PENDING(),
                    DMEFileList.SyncState.RUNNING() -> {
                        DMELog.i("Sync still in progress, continuing to poll for updates.")
                        scheduleNextPoll()
                    }
                    DMEFileList.SyncState.COMPLETED() -> fileListCompletionHandler?.invoke(null)
                    DMEFileList.SyncState.PARTIAL() -> fileListCompletionHandler?.invoke(DMEAPIError.PartialSync())
                    else -> Unit
                }

                activeSyncState = syncStatus
            }

        }, delay)
    }

    private fun completeDeliveryOfSessionData(error: DMEError?) {

        when {
            error != null -> DMELog.e("" +
                    "An error occurred whilst fetching session data. Error: ${error.message}")
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