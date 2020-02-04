package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
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
import me.digi.sdk.utilities.jwt.DMEAuthCodeExchangeRequestJWT
import me.digi.sdk.utilities.jwt.DMEPreauthorizationRequestJWT
import me.digi.sdk.utilities.jwt.DMETriggerDataQueryRequestJWT
import me.digi.sdk.utilities.jwt.RefreshCredentialsRequestJWT
import kotlin.math.max

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

    private val compositeDisposable = CompositeDisposable()

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

        fun requestSession(req: DMESessionRequest) = Single.create<DMESession> {
            sessionManager.getSession(req) { session, error ->
                when {
                    error != null -> it.onError(error)
                    session != null -> it.onSuccess(session)
                    else -> it.onError(java.lang.IllegalArgumentException())
                }
            }
        }

        fun requestPreauthorizationCode() = SingleTransformer<DMESession, DMESession> {

            it.flatMap { session ->

                val codeVerifier = DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))
                session.metadata[context.getString(R.string.key_code_verifier)] = codeVerifier

                val jwt = DMEPreauthorizationRequestJWT(configuration.appId, configuration.contractId, codeVerifier)

                val signingKey = DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                val authHeader = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.getPreauthorizationCode(authHeader))
                    .map {
                        session.apply {
                            preauthorizationCode = it.preauthorizationCode
                        }
                    }
            }
        }

        fun requestConsent(fromActivity: Activity) = SingleTransformer<DMESession, DMESession> {
            it.flatMap { session ->
                Single.create<DMESession> { emitter ->
                    nativeConsentManager.beginAuthorization(fromActivity) { session, error ->
                        when {
                            error != null -> emitter.onError(error)
                            session != null -> emitter.onSuccess(session)
                            else -> emitter.onError(java.lang.IllegalArgumentException())
                        }
                    }
                }
            }
        }

        fun exchangeAuthorizationCode() = SingleTransformer<DMESession, Pair<DMESession, DMEOAuthToken>> {
            it.flatMap { session ->

                val codeVerifier = session.metadata[context.getString(R.string.key_code_verifier)].toString()
                val jwt = DMEAuthCodeExchangeRequestJWT(configuration.appId, configuration.contractId, session.authorizationCode!!, codeVerifier)

                val signingKey = DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                val authHeader = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                    .map { token ->
                        Pair(session, DMEOAuthToken(token))
                    }
            }
        }

        fun triggerDataQuery() = SingleTransformer<Pair<DMESession, DMEOAuthToken>, Pair<DMESession, DMEOAuthToken>> {
            it.flatMap { result ->
                val jwt = DMETriggerDataQueryRequestJWT(configuration.appId, configuration.contractId, result.first.key, result.second.accessToken)
                val signingKey = DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                val authHeader = jwt.sign(signingKey).tokenize()
                apiClient.makeCall(apiClient.argonService.triggerDataQuery(authHeader))
                    .map { result }
            }
        }

        fun refreshCredentials() = SingleTransformer<Pair<DMESession, DMEOAuthToken>, Pair<DMESession, DMEOAuthToken>> {
            it.flatMap { result ->
                val jwt = RefreshCredentialsRequestJWT(configuration.appId, configuration.contractId, result.second.refreshToken)
                val signingKey = DMEKeyTransformer.javaPrivateKeyFromHex(configuration.privateKeyHex)
                val authHeader = jwt.sign(signingKey).tokenize()
                apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                    .map { result }
            }
        }

        // Defined above are a number of 'modules' that are used within the Cyclic CA flow.
        // These can be combined in various ways as the auth state demands.
        // See the flow below for details.

        val sessionReq = DMESessionRequest(configuration.appId, configuration.contractId, DMESDKAgent(), "gzip", scope)
        var activeCredentials = credentials

        // First, we get a session as normal.
        requestSession(sessionReq)

            // Next, we check if any credentials were supplied (for access restoration).
            // If not, we kick the user out to digi.me to authorise normally.
            .let {
                if (activeCredentials != null) {
                    it.map { Pair(it, activeCredentials!!) }
                }
                else {
                    it.compose(requestPreauthorizationCode())
                    .compose(requestConsent(fromActivity))
                    .compose(exchangeAuthorizationCode())
                    .doOnSuccess {
                        activeCredentials = it.second
                    }
                }
            }

            // At this point, we have a session and a set of credentials, so we can trigger
            // the data query to 'pair' the credentials with the session.
            .compose(triggerDataQuery())
            .onErrorResumeNext { error ->

                // If an error is encountered from this call, we inspect it to see if it's an
                // 'InvalidToken' error, meaning that the ACCESS token has expired.
                if (error is DMEAPIError.Server && error.code == "InvalidToken") {

                    // If so, we take the active session and expired credentials and try to refresh them.
                    Single.just(Pair(nativeConsentManager.sessionManager.currentSession!!, activeCredentials!!))
                        .compose(refreshCredentials())
                        .doOnSuccess { activeCredentials = it.second }
                        .onErrorResumeNext { error ->

                            // If an error is encountered from this call, we inspect it to see if it's an
                            // 'InvalidToken' error, meaning that the REFRESH token has expired.
                            if (error is DMEAPIError.Server && error.code == "InvalidToken") {

                                // If so, we need to obtain a new set of credentials from the digi.me
                                // application. Process the flow as before, for ongoing access.
                                Single.just(nativeConsentManager.sessionManager.currentSession!!)
                                    .compose(requestPreauthorizationCode())
                                    .compose(requestConsent(fromActivity))
                                    .compose(exchangeAuthorizationCode())
                                    .doOnSuccess {
                                        activeCredentials = it.second
                                    }

                                    // Once new credentials are obtained, re-trigger the data query.
                                    // If it fails here, credentials are not the issue. The error
                                    // will be propagated down to the callback as normal.
                                    .compose(triggerDataQuery())
                            }
                            else {
                                Single.error(error)
                            }
                        }
                }
                else {
                    Single.error(error)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                completion(result.first, result.second, null)
            }, { error ->
                completion(null, null, error.let { it as? DMEError } ?: DMEAPIError.Generic())
            })
            .addTo(compositeDisposable)
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