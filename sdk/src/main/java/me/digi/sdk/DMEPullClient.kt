package me.digi.sdk

import android.app.*
import android.content.Context
import android.os.Handler
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import me.digi.sdk.api.helpers.DMEAuthCodeRedemptionHelper
import me.digi.sdk.callbacks.*
import me.digi.sdk.callbacks.DMEFileListCompletion
import me.digi.sdk.entities.*
import me.digi.sdk.entities.api.DMEJsonWebToken
import me.digi.sdk.entities.api.DMESessionRequest
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.interapp.managers.DMEGuestConsentManager
import me.digi.sdk.interapp.managers.DMENativeConsentManager
import me.digi.sdk.ui.ConsentModeSelectionDialogue
import me.digi.sdk.utilities.DMEFileListItemCache
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.PreauthorizationRequestJWT
import kotlin.math.max
import kotlin.math.min

class DMEPullClient(val context: Context, val configuration: DMEPullConfiguration): DMEClient(context, configuration) {

    private val nativeConsentManager: DMENativeConsentManager by lazy { DMENativeConsentManager(sessionManager, configuration.appId) }
    private val guestConsentManager: DMEGuestConsentManager by lazy { DMEGuestConsentManager(sessionManager, configuration.baseUrl) }

    private var activeFileDownloadHandler: DMEFileContentCompletion? = null
    private var activeSessionDataFetchCompletionHandler: DMEFileListCompletion? = null
    private var fileListUpdateHandler: DMEIncrementalFileListUpdate? = null
    private var fileListCompletionHandler: DMEFileListCompletion? = null
    private var fileListItemCache: DMEFileListItemCache? = null
    private var latestFileList: DMEFileList? = null
    private var activeSyncStatus: DMEFileList.SyncStatus? = null
        set(value) {
            val previousValue = field
            if (previousValue != value && previousValue != null && value != null)
                DMELog.d("Sync syncStatus changed. Previous: ${previousValue.rawValue}. New: ${value.rawValue}.")

            if (activeDownloadCount == 0) {
                when (value) {
                    DMEFileList.SyncStatus.COMPLETED(),
                    DMEFileList.SyncStatus.PARTIAL() -> completeDeliveryOfSessionData(null)
                    else -> Unit
                }
            }

            field = value
        }
    private var activeDownloadCount = 0
        set(value) {
            if (value == 0) {
                when (activeSyncStatus) {
                    DMEFileList.SyncStatus.COMPLETED(),
                    DMEFileList.SyncStatus.PARTIAL() -> completeDeliveryOfSessionData(null)
                    else -> Unit
                }
            }

            field = value
        }

    private var stalePollCount = 0

    @JvmOverloads
    fun authorize(fromActivity: Activity, scope: DMEDataRequest? = null, completion: DMEAuthorizationCompletion) {

        DMELog.i("Launching user consent request.")

        val req = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", scope)
        sessionManager.getSession(req) { session, error ->

            if (session != null) {
                when (Pair(DMEAppCommunicator.getSharedInstance().canOpenDMEApp(), configuration.guestEnabled)) {
                    Pair(true, true),
                    Pair(true, false) -> nativeConsentManager.beginAuthorization(fromActivity, completion)
                    Pair(false, true) -> {
                        val consentModeDialogue = ConsentModeSelectionDialogue()
                        consentModeDialogue.configureHandler(object: ConsentModeSelectionDialogue.DecisionHandler {
                            override fun installDigiMe() {
                                DMEAppCommunicator.getSharedInstance().requestInstallOfDMEApp(fromActivity) {
                                    nativeConsentManager.beginAuthorization(fromActivity, completion)
                                }
                            }

                            override fun shareAsGuest() {
                                guestConsentManager.beginGuestAuthorization(fromActivity, completion)
                            }
                        })
                        consentModeDialogue.show(fromActivity.fragmentManager, "ConsentModeSelection")
                    }
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

    @JvmOverloads
    fun authorizeOngoingAccess(fromActivity: Activity, scope: DMEDataRequest? = null, credentials: DMEOAuthToken? = null, completion: DMEOngoingAuthorizationCompletion) {

        fun requestPreauthorizationCode(): Single<String> {

            val signingKey = DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)

            val codeVerifier = DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))
            val jwt = PreauthorizationRequestJWT(configuration.appId, configuration.contractId, codeVerifier)

            val authHeader = jwt.sign(signingKey).tokenize()

            return apiClient.makeCall(apiClient.argonService.getPreauthorizionCode(authHeader))
                .map { it.preauthorizationCode }
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

        val sessionReq = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", scope)

        val disposable = requestSession(sessionReq)
            .flatMap { session ->
                requestPreauthorizationCode().map { Pair(session, it) }
            }
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
                    else -> it.first
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                DMELog.i(result.toString())
            }, { error ->
                DMELog.e(error.toString())
            })
    }

    fun getSessionData(downloadHandler: DMEFileContentCompletion, completion: DMEFileListCompletion) {

        DMELog.i("Starting fetch of session data.")


        activeFileDownloadHandler = downloadHandler
        activeSessionDataFetchCompletionHandler = completion

        getSessionFileList({ _, updatedFileIds ->

            updatedFileIds.forEach {

                activeDownloadCount++
                DMELog.d("Downloading file with ID: $it.")

                getSessionData(it) { file, error ->

                    when {
                        file != null -> DMELog.i("Successfully downloaded updates for file with ID: $it.")
                        else -> DMELog.e("Failed to download updates for file with ID: $it.")
                    }

                    downloadHandler.invoke(file, error)
                    activeDownloadCount--
                }
            }

        }) { fileList, error ->
            if (error != null) {
                completion(fileList, error) // We only want to push this if the error exists, else
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

    fun getSessionFileList(updateHandler: DMEIncrementalFileListUpdate, completion: DMEFileListCompletion) {

        fileListUpdateHandler = updateHandler
        fileListCompletionHandler = { fileList, error ->
            val err = if (error is DMESDKError.FileListPollingTimeout) null else error
            completion(fileList, err)
            if (activeFileDownloadHandler == null && activeSessionDataFetchCompletionHandler == null) {
                completeDeliveryOfSessionData(err)
            }
        }

        if (activeSyncStatus == null) {
            // Init syncStatus.
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

        val delay = (if (immediately) 0 else (max(configuration.pollInterval, 3) * 1000).toLong())
        Handler().postDelayed({

            DMELog.d("Fetching file list.")
            getFileList { fileList, listFetchError ->

                when {
                    fileList != null -> DMELog.d("File list obtained; Sync syncStatus is ${fileList.syncStatus.rawValue}.")
                    listFetchError != null -> DMELog.d("Error fetching file list: ${listFetchError.message}.")
                }

                val syncStatus = fileList?.syncStatus ?: DMEFileList.SyncStatus.RUNNING()

                latestFileList = fileList
                val updatedFileIds = fileListItemCache?.updateCacheWithItemsAndDeduceChanges(fileList?.fileList.orEmpty()).orEmpty()
                DMELog.i("${fileList?.fileList.orEmpty().count()} files discovered. Of these, ${updatedFileIds.count()} have updates and need downloading.")

                if (updatedFileIds.count() > 0 && fileList != null) {
                    fileListUpdateHandler?.invoke(fileList, updatedFileIds)
                    stalePollCount = 0
                }
                else if (++stalePollCount == max(configuration.maxStalePolls, 20)) {
                    fileListCompletionHandler?.invoke(fileList, DMESDKError.FileListPollingTimeout())
                    return@getFileList
                }

                when (syncStatus) {
                    DMEFileList.SyncStatus.PENDING(),
                    DMEFileList.SyncStatus.RUNNING() -> {
                        DMELog.i("Sync still in progress, continuing to poll for updates.")
                        scheduleNextPoll()
                    }
                    DMEFileList.SyncStatus.COMPLETED(),
                    DMEFileList.SyncStatus.PARTIAL() -> fileListCompletionHandler?.invoke(fileList, null)
                    else -> Unit
                }

                activeSyncStatus = syncStatus
            }

        }, delay)
    }

    private fun completeDeliveryOfSessionData(error: DMEError?) {

        when {
            error != null -> DMELog.e("" +
                    "An error occurred whilst fetching session data. Error: ${error.message}")
            else -> DMELog.i("Session data fetch completed successfully.")
        }

        activeSessionDataFetchCompletionHandler?.invoke(latestFileList, error)

        // Clear syncStatus.
        fileListItemCache = null
        latestFileList = null
        activeFileDownloadHandler = null
        activeSessionDataFetchCompletionHandler = null
        activeSyncStatus = null
        activeDownloadCount = 0
    }
}