package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Base64
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
import me.digi.sdk.interapp.managers.SaasConsentManager
import me.digi.sdk.utilities.DMECompressor
import me.digi.sdk.utilities.DMEFileListItemCache
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEDataDecryptor
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.*
import java.security.PrivateKey
import java.util.*
import kotlin.math.max

class DMEPullClient(val context: Context, val configuration: DMEPullConfiguration) : DMEClient(
    context,
    configuration
) {

    private val authConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(
            configuration.baseUrl, type = "authorize"
        )
    }
    private val onboardConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(
            configuration.baseUrl, type = "onboard"
        )
    }

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

    fun authorizeOngoingAccess(
        fromActivity: Activity,
        scope: DMEDataRequest? = null,
        credentials: DMETokenExchange? = null,
        completion: DMESaasOngoingAuthorizationCompletion
    ) {

        fun requestPreAuthCode(): Single<Pair<Session, Payload>> = Single.create { emitter ->

            val codeVerifier =
                DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))

            val jwt = if (credentials != null)
                DMEPreauthorizationRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    codeVerifier,
                    credentials.accessToken.value
                ) else DMEPreauthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.getPreAuthorizationCode(authHeader)) { response, error ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload = Gson().fromJson(payloadJson, Payload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] = codeVerifier

                        val result: Pair<Session, Payload> = Pair(response.session, payload)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(java.lang.IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity): SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> =
            SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> {
                it.flatMap { response ->
                    Single.create { emitter ->
                        response.second.preAuthorizationCode?.let {
                            authConsentManager.beginConsentAction(fromActivity, it) { authSession, error ->
                                when {
                                    error != null -> emitter.onError(error)
                                    authSession != null -> emitter.onSuccess(
                                        Pair(
                                            response.first,
                                            authSession
                                        )
                                    )
                                    else -> emitter.onError(java.lang.IllegalArgumentException())
                                }
                            }
                        }
                    }
                }
            }

        fun exchangeAuthorizationCode(): SingleTransformer<Pair<Session, AuthSession>, Pair<Session, DMETokenExchange>> =
            SingleTransformer<Pair<Session, AuthSession>, Pair<Session, DMETokenExchange>> {
                it.flatMap { response: Pair<Session, AuthSession> ->

                    val codeVerifier =
                        response.first.metadata[context.getString(R.string.key_code_verifier)].toString()

                    val jwt = DMEAuthCodeExchangeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        response.second.code!!,
                        codeVerifier
                    )

                    val signingKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.exchangeAuthToken(authHeader))
                        .map { exchangeToken: ExchangeTokenJWT ->

                            val chunks: List<String> = exchangeToken.token.split(".")
                            val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange = Gson().fromJson(payloadJson, DMETokenExchange::class.java)

                            Pair(response.first, tokenExchange)
                        }
                }
            }

        fun triggerDataQuery(): SingleTransformer<Pair<Session, DMETokenExchange>, Pair<Session, DMETokenExchange>> =
            SingleTransformer<Pair<Session, DMETokenExchange>, Pair<Session, DMETokenExchange>> {
                it.flatMap { result: Pair<Session, DMETokenExchange> ->

                    val jwt = DMETriggerDataQueryRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        result.second.accessToken.value
                    )

                    val signingKey: PrivateKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.triggerDataQuery(authHeader))
                        .map { response: DataQueryResponse ->
                            Pair(response.session, result.second)
                        }
                }
            }

        fun refreshCredentials(): SingleTransformer<Pair<Session, DMETokenExchange>, Pair<Session, DMETokenExchange>> =
            SingleTransformer<Pair<Session, DMETokenExchange>, Pair<Session, DMETokenExchange>> {
                it.flatMap { result ->

                    val jwt = RefreshCredentialsRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        result.second.refreshToken.value
                    )

                    val signingKey: PrivateKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                        .map { exchangeToken ->

                            val chunks: List<String> = exchangeToken.token.split(".")
                            val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                            val tokenExchange = Gson().fromJson(payloadJson, DMETokenExchange::class.java)

                            Pair(result.first, tokenExchange)
                        }
                }
            }

        // Defined above are a number of 'modules' that are used within the Cyclic CA flow.
        // These can be combined in various ways as the auth state demands.
        // See the flow below for details.

        var activeCredentials: DMETokenExchange? = credentials

        // First, we request pre-auth code
        requestPreAuthCode()
            // Next, we check if any credentials were supplied (for access restoration).
            // If not, we kick the user out of the flow to authorise normally.
            .let { preAuthResponse ->
                if (activeCredentials != null) {
                    preAuthResponse.map { Pair(it.first, activeCredentials!!) }
                } else {
                    preAuthResponse
                        .compose(requestConsent(fromActivity))
                        .compose(exchangeAuthorizationCode())
                        .doOnSuccess { token ->
                            activeCredentials = token.second
                        }
                }
            }
            // At this point, we have a session and a set of credentials, so we can trigger
            // the data query to 'pair' the credentials with the session.
            .compose(triggerDataQuery())
            .onErrorResumeNext { error ->

                // If an error is encountered from this call, we inspect it to see if it's an 'InternalServerError'
                // error, meaning that implicit sync was triggered wor a removed deviceId (library changed).
                // We process the consent flow for ongoing access
                if (error is DMEAPIError && error.code == "InternalServerError") {

                    requestPreAuthCode()
                        .compose(requestConsent(fromActivity))
                        .compose(exchangeAuthorizationCode())
                        .doOnSuccess { activeCredentials = it.second }

                    // If an error we encountered is a "InvalidToken" error, which means that the ACCESS token
                    // has expired.
                }
                else if (error is DMEAPIError && error.code == "InvalidToken") {
                    // If so, we take the active session and expired credentials and try to refresh them.

                    requestPreAuthCode()
                        .map { Pair(it.first, activeCredentials!!) }
                        .compose(refreshCredentials())
                        .doOnSuccess { activeCredentials = it.second }
                        .onErrorResumeNext { error ->

                            // If an error is encountered from this call, we inspect it to see if it's an
                            // 'InvalidToken' error, meaning that the REFRESH token has expired.
                            if (error is DMEAPIError && error.code == "InvalidToken") {
                                // If so, we need to obtain a new set of credentials from the digi.me
                                // application. Process the flow as before, for ongoing acces, provided
                                // that auto-recover is enabled. If not, we throw a specific error and
                                // exit the flow.
                                if (configuration.autoRecoverExpiredCredentials) {
                                    requestPreAuthCode()
                                        .compose(requestConsent(fromActivity))
                                        .compose(exchangeAuthorizationCode())
                                        .doOnSuccess { activeCredentials = it.second }

                                        // Once new credentials are obtained, re-trigger the data query.
                                        // If it fails here, credentials are not the issue. The error
                                        // will be propagated down to the callback as normal.
                                        .compose(triggerDataQuery())
                                }
                                else Single.error(DMEAuthError.TokenExpired())
                            } else Single.error(error)
                        }
                }
                else Single.error(error)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: Pair<Session, DMETokenExchange> ->
                    println("Session: ${result.first}")
                    println("Token: ${result.second}")
                    sessionManager.newSession = result.first
                    completion.invoke(result.second, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError } ?: DMEAPIError.GENERIC(
                            0,
                            error.localizedMessage
                        ))
                }
            )
            .addTo(compositeDisposable)
    }

    fun getServicesForContractId(contractId: String, completion: DMEServicesForContract) {
        DMELog.i("Fetching services for contract")

        apiClient.argonService.getServicesForContract(contractId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    completion.invoke(it, null)
                },
                onError = {
                    completion.invoke(null, DMEAuthError.ErrorWithMessage(it.localizedMessage ?: "Could not fetch services. Something went wrong"))
                }
            )
    }

    fun authorize(fromActivity: Activity, completion: AuthorizationCompletion) {

        fun requestPreAuthCode(): Single<Pair<Session, Payload>> = Single.create { emitter ->

            val codeVerifier =
                DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))

            val jwt = DMEPreauthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.getPreAuthorizationCode(authHeader)) { response, error ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload = Gson().fromJson(payloadJson, Payload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] = codeVerifier

                        val result = Pair(response.session, payload)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(java.lang.IllegalArgumentException())
                }
            }
        }

        fun requestConsent(fromActivity: Activity): SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> =
            SingleTransformer<Pair<Session, Payload>, Pair<Session, AuthSession>> {
                it.flatMap { response ->
                    Single.create { emitter ->
                        response.second.preAuthorizationCode?.let {
                            authConsentManager.beginConsentAction(fromActivity, it) { authSession, error ->
                                when {
                                    error != null -> emitter.onError(error)
                                    authSession != null -> emitter.onSuccess(
                                        Pair(response.first, authSession)
                                    )
                                    else -> emitter.onError(java.lang.IllegalArgumentException())
                                }
                            }
                        }
                    }
                }
            }

        requestPreAuthCode()
            .compose(requestConsent(fromActivity))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: Pair<Session, AuthSession> ->
                    sessionManager.newSession = result.first
                    completion.invoke(result.second, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError } ?: DMEAPIError.GENERIC(
                            0,
                            error.localizedMessage
                        )
                    )
                }
            )
            .addTo(compositeDisposable)
    }

    fun onboardService(
        fromActivity: Activity,
        serviceId: String,
        codeValue: String?,
        completion: OnboardingCompletion
    ) {
        DMELog.i("Launching user onboarding request.")
        codeValue?.let {
            onboardConsentManager.beginOnboardAction(fromActivity, it, serviceId, completion)
        }
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

    private fun getSessionData(fileId: String, completion: DMEFileContentCompletion) {

        val currentSession = sessionManager.newSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            apiClient.argonService.getFileBytes(currentSession.key, fileId)
                .map { response ->
                    val headers = response.headers()["X-Metadata"]
                    val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                    val payloadHeader = Gson().fromJson(headerString, HeaderMetadata::class.java)

                    val result: ByteArray = response.body()?.byteStream()?.readBytes() as ByteArray

                    val contentBytes: ByteArray = DMEDataDecryptor.dataFromEncryptedBytes(result, configuration.privateKeyHex)

                    val compression: String = try { payloadHeader.compression } catch(e: Throwable) { DMECompressor.COMPRESSION_NONE }
                    val decompressedContentBytes: ByteArray = DMECompressor.decompressData(contentBytes, compression)

                    DMEFile().copy(fileContent = String(decompressedContentBytes))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { completion.invoke(it, null) },
                    onError = {
                        completion.invoke(
                            null,
                            DMEAuthError.ErrorWithMessage(it.localizedMessage ?: "Unknown error occurred")
                        )
                    }
                )
        }
        else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    private fun getSessionFileList(
        updateHandler: DMEIncrementalFileListUpdate,
        completion: DMEFileListCompletion
    ) {

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

        val currentSession = sessionManager.newSession

        if (currentSession != null && sessionManager.isSessionValid()) {
            apiClient.makeCall(apiClient.argonService.getFileList(currentSession.key), completion)
        }
        else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    // TODO: Handle better if possible
    fun getSessionAccounts(completion: DMEAccountsCompletion) {

        val currentSession = sessionManager.newSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            DMELog.i("Starting account fetch.")

            apiClient.makeCall(apiClient.argonService.getFile(currentSession.key, "accounts.json")) { file, error ->

                if (file == null) {
                    DMELog.e("Failed to fetch accounts. Error: ${error?.message}")
                    completion(null, error)
                }

//                val accountsFileJSON = file?.fileContentAs(DMEAccountList::class.java)
//                val accounts = accountsFileJSON?.accounts

//                DMELog.i("Successfully fetched accounts: ${accounts?.map { it.id }}")
//                completion(accounts, error)
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
                val updatedFileIds = fileListItemCache?.updateCacheWithItemsAndDeduceChanges(
                    fileList?.fileList.orEmpty()
                ).orEmpty()

                DMELog.i(
                    "${
                        fileList?.fileList.orEmpty().count()
                    } files discovered. Of these, ${updatedFileIds.count()} have updates and need downloading."
                )

                if (updatedFileIds.count() > 0 && fileList != null) {
                    fileListUpdateHandler?.invoke(fileList, updatedFileIds)
                    stalePollCount = 0
                } else if (++stalePollCount == max(configuration.maxStalePolls, 20)) {
                    fileListCompletionHandler?.invoke(
                        fileList,
                        DMESDKError.FileListPollingTimeout()
                    )
                    return@getFileList
                }

                when (syncStatus) {
                    DMEFileList.SyncStatus.PENDING(),
                    DMEFileList.SyncStatus.RUNNING() -> {
                        DMELog.i("Sync still in progress, continuing to poll for updates.")
                        scheduleNextPoll()
                    }
                    DMEFileList.SyncStatus.COMPLETED(),
                    DMEFileList.SyncStatus.PARTIAL() -> fileListCompletionHandler?.invoke(
                        fileList,
                        null
                    )
                    else -> Unit
                }

                activeSyncStatus = syncStatus
            }

        }, delay)
    }

    private fun completeDeliveryOfSessionData(error: DMEError?) {

        when {
            error != null -> DMELog.e(
                "" +
                        "An error occurred whilst fetching session data. Error: ${error.message}"
            )
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