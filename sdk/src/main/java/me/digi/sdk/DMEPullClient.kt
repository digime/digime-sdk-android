package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.SingleTransformer
import me.digi.sdk.api.helpers.DMEAuthCodeRedemptionHelper
import me.digi.sdk.callbacks.*
import me.digi.sdk.callbacks.DMEFileListCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMEJsonWebToken
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
            if (value == 0) {
                when (activeSyncState) {
                    DMEFileList.SyncState.COMPLETED() -> completeDeliveryOfSessionData(null)
                    DMEFileList.SyncState.PARTIAL() -> completeDeliveryOfSessionData(DMEAPIError.PartialSync())
                    else -> Unit
                }
            }

            field = value
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

    fun authorizeOngoingAccess(fromActivity: Activity, accessToken: String?, refreshToken: String?, completion: DMEOngoingAuthorizationCompletion) =
        authorizeOngoingAccess(fromActivity, null, accessToken, refreshToken, completion)

    fun authorizeOngoingAccess(fromActivity: Activity, scope: DMEDataRequest?, accessToken: String?, refreshToken: String?, completion: DMEOngoingAuthorizationCompletion) {

        fun requestPreauthorizationCode(helper: DMEAuthCodeRedemptionHelper): Single<String> {
            return apiClient.makeCall(apiClient.argonService.getPreauthorizionCode(helper.buildForPreAuthRequest(configuration.contractId)))
                .map {
                    when {
                        it.payload is DMEJsonWebToken.Payload.OAuth -> it.payload.accessToken
                        else -> throw IllegalArgumentException()
                    }
                }
        }

        fun requestSession(req: DMESessionRequest) = Single.create<DMESession> {
            sessionManager.getSession(req) { session, error ->
                when {
                    error != null -> it.onError(error)
                    session != null -> it.onSuccess(session)
                    else -> it.onError(java.lang.IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity, lambda: (fromActivity: Activity, completion: DMEAuthorizationCompletion) -> Unit) = Single.create<DMESession> {
            lambda.invoke(fromActivity) { session, error ->
                when {
                    error != null -> it.onError(error)
                    session != null -> it.onSuccess(session)
                    else -> it.onError(java.lang.IllegalArgumentException())
                }
            }
        }

        fun redeemForAuthCode(helper: DMEAuthCodeRedemptionHelper, preauthCode: String): Single<String> {
            val req = helper.buildForPreAuthRequest(configuration.contractId)
        }

        val sessionReq = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", scope)
        val authHelper = DMEAuthCodeRedemptionHelper()

        requestSession(sessionReq)
            .map { session -> requestPreauthorizationCode(authHelper).map { Pair(session, it) } }
            .map {
                when (Pair(DMEAppCommunicator.getSharedInstance().canOpenDMEApp(), configuration.guestEnabled)) {
                    Pair(true, true),
                    Pair(true, false) -> requestConsent(fromActivity, nativeConsentManager::beginAuthorization)
                    Pair(false, true) -> requestConsent(fromActivity, guestConsentManager::beginGuestAuthorization)
                    Pair(false, false) -> {
                        DMEAppCommunicator.getSharedInstance().requestInstallOfDMEApp(fromActivity) {
                            requestConsent(fromActivity, nativeConsentManager::beginAuthorization)
                        }
                    }
                    else -> it.map { it.first }
                }
            }
            .ma


        sessionManager.getSession(sessionReq) { session, error ->

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

        val ongoingAuthHelper = DMEAuthCodeRedemptionHelper()

        requestPreauthorizationCode()
            .compose(authorize())
            .co

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
                    DMEFileList.SyncState.RUNNING() -> {
                        DMELog.i("Sync still in progress, continuing to poll for updates.")
                        scheduleNextPoll()
                    }
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

                activeFileDownloadHandler?.invoke(file, error)
                activeDownloadCount--
            }
        }
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