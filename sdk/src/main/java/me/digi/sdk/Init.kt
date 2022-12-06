package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleSource
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.sdk.*
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.PreAuthorizationCodePayload
import me.digi.sdk.entities.payload.TokenReferencePayload
import me.digi.sdk.entities.request.*
import me.digi.sdk.entities.response.*
import me.digi.sdk.interapp.managers.SaasConsentManager
import me.digi.sdk.utilities.Compressor
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.FileListItemCache
import me.digi.sdk.utilities.crypto.*
import me.digi.sdk.utilities.jwt.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.security.PrivateKey
import kotlin.math.max

class Init(
    val context: Context,
    val configuration: DigiMeConfiguration
) : Client(context = context, config = configuration) {

    private val authorizeConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(configuration.baseUrl, context.getString(R.string.labelSaasAuthorize))
    }

    private val onboardConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(
            configuration.baseUrl,
            type = context.getString(R.string.labelSaasOnboarding)
        )
    }

    private var activeFileDownloadHandler: FileContentCompletion? = null
    private var activeSessionDataFetchCompletionHandler: FileListCompletion? = null
    private var fileListUpdateHandler: IncrementalFileListUpdate? = null
    private var fileListCompletionHandler: FileListCompletion? = null
    private var fileListItemCache: FileListItemCache? = null
    private var latestFileList: FileList? = null

    var activeSyncStatus: FileList.SyncStatus? = null
        set(value) {
            val previousValue = field
            if (previousValue != value && previousValue != null && value != null)
                DMELog.d("Sync syncStatus changed. Previous: ${previousValue.rawValue}. New: ${value.rawValue}.")

            if (activeDownloadCount == 0) {
                when (value) {
                    FileList.SyncStatus.COMPLETED(),
                    FileList.SyncStatus.PARTIAL() -> completeDeliveryOfSessionData(null)
                    else -> Unit
                }
            }

            field = value
        }
    var activeDownloadCount = 0
        set(value) {
            if (value == 0) {
                when (activeSyncStatus) {
                    FileList.SyncStatus.COMPLETED(),
                    FileList.SyncStatus.PARTIAL() -> completeDeliveryOfSessionData(null)
                    else -> Unit
                }
            }

            field = value
        }

    private var stalePollCount = 0
    private var isFirstRun: Boolean = false

    /**
     * Authorizes the contract configured with this digi.me instance to access to a library.
     *
     * If the user has not already authorized, will be presented with a browser window in which user consents.
     * If the user has already authorized, refreshes the authorization, if necessary
     * (which may require user consent again).
     *
     * To authorize this contract to access the same library that another contract has
     * been authorized to access, specify the contract to link to. This is useful when
     * a read contract needs to access the same library that a write contract has
     * written data to.
     *
     * Additionally for read contracts:
     * - Upon first authorization, can optionally specify a service from which user
     * can log in to an retrieve data.
     * - Creates a session during which data can be read from library.
     *
     * @param scope Options to filter which data is read from sources for this session.
     * Only used for read contracts.
     * @param serviceId Identifier of initial service to add. Only valid for first
     * authorization of read contracts where user has not previously granted consent.
     * Ignored for all subsequent calls.
     * @param completion Block called upon authorization with any errors encountered.
     */
    fun authorizeAccess(
        fromActivity: Activity,
        scope: DataRequest? = null,
        credentials: CredentialsPayload? = null,
        serviceId: String? = null,
        completion: GetAuthorizationDoneCompletion
    ) {
        requestPreAuthorizationCode(credentials, scope)
            .compose(requestConsentAccess(fromActivity, serviceId))
            .compose(requestTokenExchange())
            .onErrorResumeNext { error ->
                errorHandler(
                    fromActivity,
                    error,
                    credentials,
                    scope,
                    serviceId
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: GetTokenExchangeDone ->
                    sessionManager.updatedSession = result.consentData.session
                    isFirstRun = true

                    val response = AuthorizationResponse().copy(
                        session = result.consentData.session,
                        authResponse = result.consentData.consentResponse,
                        credentials = result.credentials
                    )

                    completion.invoke(response, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? Error }
                            ?: APIError.ErrorWithMessage(
                                error.localizedMessage ?: "Unknown error occurred"
                            ))
                }
            )
    }

    /**
     * Deletes the user's library associated with the configured contract.
     *
     * Please note that if multiple contracts are linked to the same library,
     * then 'deleteUser' will also need to be called on those contracts to remove
     * any stored credentials, in which case an error may be reported on those calls.
     *
     * @param completion Block called on completion with value true/false upon library deletion
     * or any error encountered
     */
    fun deleteUser(
        userAccessToken: String?,
        completion: UserDeleteCompletion
    ) {
        DMELog.i(context.getString(R.string.labelDeleteLibrary))

        fun deleteLibrary() = Single.create<Boolean> { emitter ->
            userAccessToken?.let {

                val jwt = UserDeletionRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    userAccessToken
                )

                val signingKey: PrivateKey =
                    KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                val authHeader: String = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.deleteUser(authHeader)) { _, error ->
                    when {
                        error != null -> emitter.onError(error)
                        else -> emitter.onSuccess(true)
                    }
                }
            }
                ?: emitter.onError(APIError.ErrorWithMessage(context.getString(R.string.labelAccessTokenInvalidOrMissing)))
        }

        deleteLibrary()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { completion.invoke(it, null) }, onError = {
                it.localizedMessage?.let { message ->
                    if (message.contains("204"))
                        completion.invoke(true, null)
                    else completion.invoke(
                        null,
                        APIError.ErrorWithMessage(message)
                    )
                }
            })
    }

    /**
     * Gets list of possible accounts from the users library.
     *
     * @param completion Block called upon completion with either list of accounts in the library;
     * or any errors encountered.
     */
    fun readAccounts(userAccessToken: String, completion: AccountsCompletion) {
        DMELog.i(context.getString(R.string.labelReadAccounts))

        val currentSession = sessionManager.updatedSession

        if (isFirstRun and (currentSession != null && sessionManager.isSessionValid())) {
            apiClient.argonService.getFileBytes(currentSession?.key!!, "accounts.json")
                .map { response: Response<ResponseBody> ->

                    val result: ByteArray = response.body()?.byteStream()?.readBytes() as ByteArray

                    val contentBytes: ByteArray =
                        DataDecryptor.dataFromEncryptedBytes(result, configuration.privateKeyHex)

                    val decompressedContentBytes: ByteArray =
                        Compressor.decompressData(contentBytes, Compressor.COMPRESSION_NONE)

                    Gson().fromJson(
                        String(decompressedContentBytes),
                        ReadAccountsResponse::class.java
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { completion.invoke(it, null) },
                    onError = {
                        completion.invoke(
                            null,
                            AuthError.ErrorWithMessage(
                                it.localizedMessage ?: "Unknown error occurred"
                            )
                        )
                    }
                )
        } else handleReadAccounts(userAccessToken, completion)
    }

    /**
     * Writes data to user's library associated with configured contract
     *
     * @param data The data to be written
     * @param userAccessToken Token to reference the existing library
     * @param completion Block called on completion with updated/returned session values with
     * delivery status or any error encountered.
     */
    fun write(
        userAccessToken: String,
        writeDataPayload: WriteDataPayload,
        completion: OngoingWriteCompletion
    ) {
        DMELog.i(context.getString(R.string.labelWriteDataToLibrary))

        if (sessionManager.isSessionValid())
            handleDataWrite(userAccessToken, writeDataPayload, completion)
        else
            updateSession { isSessionUpdated, error ->
                error?.let {
                    DMELog.e("Your session is invalid; please request a new one.")
                    completion(null, AuthError.InvalidSession())
                }
                    ?: run {
                        handleDataWrite(userAccessToken, writeDataPayload, completion)
                    }
            }
    }

    /**
     * Requests new session, and updates session manager with it.
     *
     * @param completion Block called upon completion with updated session, boolean value
     * if session was in fact updated, or any error encountered.
     */
    private fun updateSession(completion: GetSessionCompletion) {

        val sessionRequest = SessionRequest(
            configuration.appId,
            configuration.contractId,
            SdkAgent(),
            "gzip",
            null
        )

        fun requestSession(): Single<SessionResponse> =
            Single.create { emitter ->
                apiClient.makeCall(apiClient.argonService.getSession(sessionRequest)) { sessionResponse, error ->
                    when {
                        sessionResponse != null -> emitter.onSuccess(sessionResponse)
                        error != null -> emitter.onError(error)
                        else -> emitter.onError(IllegalArgumentException())
                    }
                }
            }

        requestSession()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    val session = Session().copy(key = it.key, expiry = it.expiry)
                    sessionManager.updatedSession = session
                    completion.invoke(true, null)
                },
                onError = {
                    completion.invoke(
                        false,
                        AuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    /**
     * Once a user has granted consent, adds an additional service
     *
     * @param serviceId Identifier of service to add
     * @param userAccessToken Token to reference the same library
     * @param completion Block called upon completion with any errors encountered
     */
    fun addService(
        fromActivity: Activity,
        serviceId: String,
        userAccessToken: String,
        completion: ServiceOnboardingCompletion
    ) {

        fun requestCodeReference(): Single<TokenReferencePayload> = Single.create { emitter ->
            DMELog.i(context.getString(R.string.labelReferenceOnboardingCode))

            val jwt = ReferenceCodeRequestJWT(
                configuration.appId,
                configuration.contractId,
                userAccessToken
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.getReferenceCode(authHeader)) { tokenReference, error ->
                when {
                    tokenReference != null -> {
                        val chunks: List<String> = tokenReference.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val result: TokenReferencePayload =
                            Gson().fromJson(payloadJson, TokenReferencePayload::class.java)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        fun requestOnboard(): SingleTransformer<TokenReferencePayload, Boolean> =
            SingleTransformer {
                it.flatMap { payload ->
                    Single.create { emitter ->
                        DMELog.i(context.getString(R.string.labelUserOnboardingRequest))

                        payload.referenceCode?.let { code ->
                            onboardConsentManager.beginOnboardAction(
                                fromActivity,
                                code,
                                serviceId
                            ) { error ->
                                when {
                                    error != null -> emitter.onError(error)
                                    else -> emitter.onSuccess(true)
                                }
                            }
                        }
                    }
                }
            }

        fun triggerDataQuery(): SingleTransformer<Boolean, Boolean> = SingleTransformer {
            it.flatMap {
                Single.create { emitter ->
                    DMELog.i(context.getString(R.string.labelTriggeringDataQuery))

                    val jwt = TriggerDataQueryRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        userAccessToken
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.makeCall(
                        apiClient.argonService.triggerDataQuery(
                            authHeader,
                            Pull()
                        )
                    ) { response, error ->
                        when {
                            response != null -> {
                                sessionManager.updatedSession = response.session
                                emitter.onSuccess(true)
                            }
                            error != null -> emitter.onError(error)
                            else -> emitter.onError(IllegalArgumentException())
                        }
                    }
                }
            }
        }

        requestCodeReference()
            .compose(requestOnboard())
            .compose(triggerDataQuery())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { completion.invoke(null) },
                onError = {
                    activeSyncStatus = null
                    completion.invoke(
                        AuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    /**
     * Get a list of possible services a user can add to their digi.me library
     *
     * @param completion Block called upon completion with either the service list
     * or any errors encountered
     */
    fun getAvailableServices(
        contractId: String? = configuration.contractId,
        completion: AvailableServicesCompletion
    ) {
        DMELog.i(context.getString(R.string.labelAvailableServices))

        apiClient.argonService.getServicesForContract(contractId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { completion.invoke(it, null) }, onError = {
                completion.invoke(
                    null,
                    APIError.ErrorWithMessage(
                        it.localizedMessage ?: "Could not fetch services. Something went wrong"
                    )
                )
            })
    }

    /**
     * Fetches content for all the files limited by read options.
     *
     * An attempt is made to fetch each requested file and the result of the attempt
     * is passed back via the download handler.
     * As download requests are asynchronous, the download handler may be called
     * concurrently, so the handler implementation should allow for this.
     *
     * If this method is called while files are being read, an error denoting this
     * will be immediately returned in the completion block of this subsequent call
     * and will not affect current calls.
     *
     * For the service-based data sources, will also attempt to retrieve any new data
     * directly from the services.
     *
     * @param downloadHandler Handler called after every file fetch attempt finishes.
     * Either contains the file or an error if fetch failed.
     * @param completion Block called when fetching all files has completed. Contains
     * final list of files or an error if reading file list failed.
     */
    fun readAllFiles(
        scope: DataRequest? = null,
        userAccessToken: String,
        downloadHandler: FileContentCompletion,
        completion: FileListCompletion
    ) {

        val currentSession = sessionManager.updatedSession

        if (isFirstRun and (currentSession != null && sessionManager.isSessionValid())) {
            handleContinuousDataDownload(userAccessToken, downloadHandler, completion)
        } else {
            handleCyclicDataDownload(scope, userAccessToken, downloadHandler, completion)
        }
    }

    /**
     * Get list of possible files from the users library.
     *
     * @param completion Block called upon completion with either list of files in the library;
     * returned as json objects, or any errors encountered.
     */
    fun readFileList(userAccessToken: String, completion: FileListCompletion) {

        val currentSession = sessionManager.updatedSession

        if ((currentSession != null && sessionManager.isSessionValid()) and (activeSyncStatus != FileList.SyncStatus.COMPLETED() && activeSyncStatus != FileList.SyncStatus.PARTIAL())) {
            apiClient.makeCall(
                apiClient.argonService.getFileList(currentSession?.key!!),
                completion
            )
        } else handleFileList(userAccessToken, completion)
    }

    /**
     * Get a file content by file ID.
     *
     * @param fileId ID for specific file
     * @param completion Block called upon completion with either file or any errors encountered.
     */
    fun readFile(userAccessToken: String, fileId: String, completion: FileContentBytesCompletion) {

        val currentSession = sessionManager.updatedSession

        if (isFirstRun and (currentSession != null && sessionManager.isSessionValid())) {
            apiClient.argonService.getFileBytes(currentSession?.key!!, fileId)
                .map { response ->
                    val headers = response.headers()["X-Metadata"]
                    val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                    val payloadHeader =
                        Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                    val result: ByteArray = response.body()?.byteStream()?.readBytes() as ByteArray

                    val contentBytes: ByteArray =
                        DataDecryptor.dataFromEncryptedBytes(result, configuration.privateKeyHex)

                    val compression: String = try {
                        payloadHeader.compression
                    } catch (e: Throwable) {
                        Compressor.COMPRESSION_NONE
                    }
                    val decompressedContentBytes: ByteArray =
                        Compressor.decompressData(contentBytes, compression)

                    FileItemBytes().copy(fileContent = decompressedContentBytes)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { completion.invoke(it, null) },
                    onError = {
                        completion.invoke(
                            null,
                            AuthError.ErrorWithMessage(
                                it.localizedMessage ?: "Unknown error occurred"
                            )
                        )
                    }
                )
        } else handleFileItemBytes(userAccessToken, fileId, completion)
    }

    private fun getSessionData(fileId: String, completion: FileContentCompletion) {
        val currentSession = sessionManager.updatedSession

        if (isFirstRun and (currentSession != null && sessionManager.isSessionValid())) {
            apiClient.argonService.getFileBytes(currentSession?.key!!, fileId)
                .map { response ->
                    val headers = response.headers()["X-Metadata"]
                    val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                    val payloadHeader =
                        Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                    val result: ByteArray = response.body()?.byteStream()?.readBytes() as ByteArray

                    val contentBytes: ByteArray =
                        DataDecryptor.dataFromEncryptedBytes(result, configuration.privateKeyHex)

                    val compression: String = try {
                        payloadHeader.compression
                    } catch (e: Throwable) {
                        Compressor.COMPRESSION_NONE
                    }
                    val decompressedContentBytes: ByteArray =
                        Compressor.decompressData(contentBytes, compression)

                    FileItem().copy(
                        fileContent = String(decompressedContentBytes),
                        fileName = fileId
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { fileItem ->
                        completion.invoke(fileItem, null)
                    },
                    onError = {
                        completion.invoke(
                            null,
                            AuthError.ErrorWithMessage(
                                it.localizedMessage ?: "Unknown error occurred"
                            )
                        )
                    }
                )
        } else handleGetSessionData(currentSession?.key!!, fileId, completion)
    }

    private fun getSessionFileList(
        userAccessToken: String,
        updateHandler: IncrementalFileListUpdate,
        completion: FileListCompletion
    ) {

        fileListUpdateHandler = updateHandler
        fileListCompletionHandler = { fileList, error ->
            val err = if (error is SDKError.FileListPollingTimeout) null else error
            completion(fileList, err)
            if (activeFileDownloadHandler == null && activeSessionDataFetchCompletionHandler == null) {
                completeDeliveryOfSessionData(err)
            }
        }

        if (activeSyncStatus == null || activeSyncStatus == FileList.SyncStatus.COMPLETED() || activeSyncStatus == FileList.SyncStatus.PARTIAL()) {
            // Init syncStatus.
            activeSyncStatus = null
            fileListItemCache = FileListItemCache()
            scheduleNextPoll(userAccessToken)
        }
    }

    private fun scheduleNextPoll(userAccessToken: String) {

        DMELog.d("Session data poll scheduled.")

        DMELog.d("Fetching file list.")
        if (activeSyncStatus != FileList.SyncStatus.COMPLETED()) {
            Handler(Looper.getMainLooper()).postDelayed({
                readFileList(userAccessToken) { fileList, listFetchError ->

                    when {
                        fileList != null -> DMELog.d("File list obtained; Sync syncStatus is ${fileList.syncStatus.rawValue}.")
                        listFetchError != null -> DMELog.d("Error fetching file list: ${listFetchError.message}.")
                    }

                    val syncStatus = fileList?.syncStatus ?: FileList.SyncStatus.RUNNING()

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
                            SDKError.FileListPollingTimeout()
                        )
                        return@readFileList
                    }

                    when (syncStatus) {
                        FileList.SyncStatus.PENDING(),
                        FileList.SyncStatus.RUNNING() -> {
                            DMELog.i("Sync still in progress, continuing to poll for updates.")
                            scheduleNextPoll(userAccessToken)
                        }
                        FileList.SyncStatus.COMPLETED(),
                        FileList.SyncStatus.PARTIAL() -> fileListCompletionHandler?.invoke(
                            fileList,
                            listFetchError
                        )
                        else -> Unit
                    }

                    activeSyncStatus = syncStatus
                }

            }, 300)
        }
    }

    private fun completeDeliveryOfSessionData(error: Error?) {

        when {
            error != null -> DMELog.e("An error occurred whilst fetching session data. Error: ${error.message}")
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

    private fun errorHandler(
        fromActivity: Activity,
        error: Throwable?,
        credentials: CredentialsPayload?,
        scope: DataRequest?,
        serviceId: String?
    ): SingleSource<out GetTokenExchangeDone>? {

        return if (error is APIError && error.code == "InternalServerError") {
            requestPreAuthorizationCode(credentials, scope)
                .compose(requestConsentAccess(fromActivity, serviceId))
                .compose(requestTokenExchange())

            // If an error we encountered is a "InvalidToken" error, which means that the ACCESS token
            // has expired.
        } else if (error is APIError && error.code == "InvalidToken") {
            // If so, we take the active session and expired credentials and try to refresh them.
            requestPreAuthorizationCode(credentials, scope)
                .map { response: GetPreAuthCodeDone ->
                    GetTokenExchangeDone()
                        .copy(
                            consentData = GetConsentDone(session = response.session),
                            credentials = credentials as CredentialsPayload
                        )
                }
                .compose(requestCredentialsRefresh())
                .onErrorResumeNext { error ->

                    // If an error is encountered from this call, we inspect it to see if it's an
                    // 'InvalidToken' error, meaning that the REFRESH token has expired.
                    if (error is APIError && error.code == "InvalidToken") {
                        // If so, we need to obtain a new set of credentials from the digi.me
                        // application. Process the flow as before, for ongoing access, provided
                        // that auto-recover is enabled. If not, we throw a specific error and
                        // exit the flow.
                        if (configuration.autoRecoverExpiredCredentials) {
                            requestPreAuthorizationCode(credentials, scope)
                                .compose(requestConsentAccess(fromActivity, serviceId))
                                .compose(requestTokenExchange())

                                // Once new credentials are obtained, re-trigger the data query.
                                // If it fails here, credentials are not the issue. The error
                                // will be propagated down to the callback as normal.
                                .compose(requestDataQuery(scope))
                        } else Single.error(AuthError.TokenExpired())
                    } else Single.error(error)
                }
        } else Single.error(error)
    }

    /**
     * /////////////////////////////////////////////
     * Defined bellow are a number of 'modules' that are used within the Cyclic flow.
     * These can be combined in various ways as the auth state demands.
     * See the flow below for details.
     * /////////////////////////////////////////////
     */

    /**
     * Gets code needed to trigger consent access.
     *
     * In case we don't have credentials, it'll create fresh library; however in case we do, it'll
     * reference the same library.
     *
     * In case we don't provide scope, it'll prepare all data, otherwise, it'll only prepare
     * scoped dataset.
     *
     * @see PreAuthorizationResponse
     */
    private fun requestPreAuthorizationCode(
        credentials: CredentialsPayload?,
        scope: DataRequest?
    ): Single<out GetPreAuthCodeDone> =
        Single.create { emitter ->

            val codeVerifier =
                ByteTransformer.hexStringFromBytes(CryptoUtilities.generateSecureRandom(64))

            val jwt = if (credentials != null)
                PreAuthorizationRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    codeVerifier,
                    credentials.accessToken.value
                )
            else PreAuthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val aaa = configuration.contractId
            val bbb = configuration.appId
            val signingKey = KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader = jwt.sign(signingKey).tokenize()

            val authScope: AuthorizationScopeRequest = scope?.let {
                AuthorizationScopeRequest(
                    actions = Actions(Pull(it)),
                    agent = Agent()
                )
            } ?: AuthorizationScopeRequest()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .getPreAuthorizationCode(authHeader, authScope)
            ) { response: PreAuthorizationResponse?, error: Error? ->
                when {
                    response != null -> {
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val payload: PreAuthorizationCodePayload =
                            Gson().fromJson(payloadJson, PreAuthorizationCodePayload::class.java)

                        response.session.metadata[context.getString(R.string.key_code_verifier)] =
                            codeVerifier

                        val result = GetPreAuthCodeDone()
                            .copy(
                                session = response.session,
                                payload = payload
                            )

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

    /**
     * It will trigger consent access.
     * In case we provide the service id, it will also trigger onboarding with authorization.
     *
     * @see GetPreAuthCodeDone
     * @see ConsentAuthResponse
     */
    private fun requestConsentAccess(
        fromActivity: Activity,
        serviceId: String?
    ): SingleTransformer<in GetPreAuthCodeDone, out GetConsentDone> =
        SingleTransformer<GetPreAuthCodeDone, GetConsentDone> {
            it.flatMap { input: GetPreAuthCodeDone ->
                Single.create { emitter ->
                    input.payload.preAuthorizationCode?.let { code ->
                        authorizeConsentManager.beginConsentAction(
                            fromActivity,
                            code,
                            serviceId
                        ) { consentResponse: ConsentAuthResponse?, error: Error? ->
                            when {
                                consentResponse?.success == true -> {
                                    val consentDone = GetConsentDone()
                                        .copy(
                                            session = input.session,
                                            consentResponse = consentResponse
                                        )

                                    emitter.onSuccess(consentDone)
                                }
                                error != null -> emitter.onError(error)
                                else -> emitter.onError(IllegalArgumentException())
                            }

                        }
                    }
                }
            }
        }

    /**
     * Once consent is completed, we trigger this method to get fresh set of credentials.
     * @see CredentialsPayload
     */
    private fun requestTokenExchange(): SingleTransformer<in GetConsentDone, out GetTokenExchangeDone> =
        SingleTransformer<GetConsentDone, GetTokenExchangeDone> {
            it.flatMap { input: GetConsentDone ->
                val codeVerifier =
                    input.session.metadata[context.getString(R.string.key_code_verifier)].toString()

                val jwt = AuthCodeExchangeRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    input.consentResponse.code!!,
                    codeVerifier
                )

                val signingKey = KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                val autHeader = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(
                    apiClient
                        .argonService
                        .exchangeAuthToken(autHeader)
                ).map { response: TokenResponse ->

                    val chunks: List<String> = response.token.split(".")
                    val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                    val credentialsPayload: CredentialsPayload =
                        Gson().fromJson(payloadJson, CredentialsPayload::class.java)

                    GetTokenExchangeDone().copy(
                        consentData = input,
                        credentials = credentialsPayload
                    )
                }
            }
        }

    /**
     * In case our credentials have expired, we'll trigger this method.
     * We extract token from response in form of payload.
     *
     * @see CredentialsPayload
     * @see GetTokenExchangeDone
     */
    private fun requestCredentialsRefresh(): SingleTransformer<in GetTokenExchangeDone, out GetTokenExchangeDone> =
        SingleTransformer<GetTokenExchangeDone, GetTokenExchangeDone> {
            it.flatMap { input: GetTokenExchangeDone ->

                val jwt = RefreshCredentialsRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    input.credentials.refreshToken.value!!
                )

                val signingKey: PrivateKey =
                    KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                val authHeader: String = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                    .map { response: TokenResponse ->

                        val chunks: List<String> = response.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val credentialsPayload =
                            Gson().fromJson(payloadJson, CredentialsPayload::class.java)

                        GetTokenExchangeDone()
                            .copy(
                                consentData = input.consentData,
                                credentials = credentialsPayload
                            )
                    }
            }
        }

    /**
     * It will 'prepare' data in the library to be synced.
     * Upon successful response, it'll return new session.
     *
     * @see Session
     */
    private fun requestDataQuery(scope: DataRequest?): SingleTransformer<in GetTokenExchangeDone, out GetTokenExchangeDone> =
        SingleTransformer<GetTokenExchangeDone, GetTokenExchangeDone> {
            it.flatMap { input: GetTokenExchangeDone ->

                val jwt = TriggerDataQueryRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    input.credentials.accessToken.value!!
                )

                val signingKey: PrivateKey =
                    KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                val authHeader: String = jwt.sign(signingKey).tokenize()

                val dataQueryScope: Pull = scope?.let { scope -> Pull(scope) } ?: Pull()

                apiClient.makeCall(
                    apiClient
                        .argonService
                        .triggerDataQuery(
                            authHeader,
                            dataQueryScope
                        )
                ).map { response: DataQueryResponse ->

                    sessionManager.updatedSession = response.session

                    GetTokenExchangeDone()
                        .copy(
                            consentData = GetConsentDone(session = response.session),
                            credentials = input.credentials
                        )
                }
            }
        }

    ////////////////
    /// Handlers ///
    ///////////////
    private fun handleReadAccounts(userAccessToken: String, completion: AccountsCompletion) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                userAccessToken
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .triggerDataQuery(authHeader, Pull())
            ) { response: DataQueryResponse?, error: Error? ->
                when {
                    response != null -> {
                        sessionManager.updatedSession = response.session
                        emitter.onSuccess(response)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        requestDataQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session

                    apiClient.argonService.getFileBytes(it.session.key, "accounts.json")
                        .map { response: Response<ResponseBody> ->

                            val result: ByteArray =
                                response.body()?.byteStream()?.readBytes() as ByteArray

                            val contentBytes: ByteArray =
                                DataDecryptor.dataFromEncryptedBytes(
                                    result,
                                    configuration.privateKeyHex
                                )

                            val decompressedContentBytes: ByteArray =
                                Compressor.decompressData(contentBytes, Compressor.COMPRESSION_NONE)

                            Gson().fromJson(
                                String(decompressedContentBytes),
                                ReadAccountsResponse::class.java
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { completion.invoke(it, null) },
                            onError = {
                                completion.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        it.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    completion.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleDataWrite(
        userAccessToken: String,
        writeDataPayload: WriteDataPayload,
        completion: OngoingWriteCompletion
    ) {
        val requestBody: RequestBody =
            writeDataPayload.content.toRequestBody(writeDataPayload.metadata.mimeType.toMediaTypeOrNull(), 0, writeDataPayload.content.size)

        val jwt = DirectImportRequestJWT(
            userAccessToken,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()

        val fileDescriptor = DirectImportMetadataRequestJWT(
            writeDataPayload.metadata
        )

        val fileDescSigningKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val fileDesc: String = fileDescriptor.sign(fileDescSigningKey).tokenize()

        apiClient.argonService.directImport(
            authHeader,
            fileDesc,
            requestBody
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    completion(
                        true,
                        null
                    ).also { DMELog.i("Successfully pushed data to postbox") }
                },
                onError = { error ->
                    when {
                        error is APIError && error.code == "InvalidToken" -> completion(
                            null,
                            APIError.GENERIC(message = "Failed to push file to postbox. Access token is invalid. Request new session.")
                        )
                        else -> {
                            DMELog.e("Failed to push file to postbox. Error: ${error.printStackTrace()} ${error.message}")
                            completion(
                                null,
                                AuthError.ErrorWithMessage(
                                    error.localizedMessage
                                        ?: context.getString(R.string.labelUnknownError)
                                )
                            )
                        }
                    }
                }
            )
    }

    data class UserAccessToken(
        val accessToken: AccessToken? = null,
    )

    data class AccessToken(
        val expires_on: Long = 0L,
        val value: String? = null
    )

    private fun handleFileList(
        userAccessToken: String,
        completion: FileListCompletion
    ) {
        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val accessToken =
                Gson().fromJson<UserAccessToken>(userAccessToken, UserAccessToken::class.java)
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                accessToken.accessToken?.value!!
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .triggerDataQuery(authHeader, Pull())
            ) { response: DataQueryResponse?, error: Error? ->
                when {
                    response != null -> {
                        sessionManager.updatedSession = response.session
                        emitter.onSuccess(response)
                    }
                    error != null -> {
                        emitter.onError(error)
                    }
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        requestDataQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session

                    apiClient.makeCall(
                        apiClient.argonService.getFileList(it.session.key),
                        completion
                    )
                },
                onError = {
                    completion.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleFileItemBytes(
        userAccessToken: String,
        fileId: String,
        completion: FileContentBytesCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                userAccessToken
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .triggerDataQuery(authHeader, Pull())
            ) { response: DataQueryResponse?, error: Error? ->
                when {
                    response != null -> {
                        sessionManager.updatedSession = response.session
                        emitter.onSuccess(response)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        requestDataQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session

                    apiClient.argonService.getFileBytes(it.session.key, fileId)
                        .map { response ->
                            val headers = response.headers()["X-Metadata"]
                            val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                            val payloadHeader =
                                Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                            val result: ByteArray =
                                response.body()?.byteStream()?.readBytes() as ByteArray

                            val contentBytes: ByteArray =
                                DataDecryptor.dataFromEncryptedBytes(
                                    result,
                                    configuration.privateKeyHex
                                )

                            val compression: String = try {
                                payloadHeader.compression
                            } catch (e: Throwable) {
                                Compressor.COMPRESSION_NONE
                            }
                            val decompressedContentBytes: ByteArray =
                                Compressor.decompressData(contentBytes, compression)

                            FileItemBytes().copy(fileContent = decompressedContentBytes)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { completion.invoke(it, null) },
                            onError = {
                                completion.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        it.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    completion.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleFileItem(
        userAccessToken: String,
        fileId: String,
        completion: FileContentCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                userAccessToken
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .triggerDataQuery(authHeader, Pull())
            ) { response: DataQueryResponse?, error: Error? ->
                when {
                    response != null -> {
                        sessionManager.updatedSession = response.session
                        emitter.onSuccess(response)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        requestDataQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session

                    apiClient.argonService.getFileBytes(it.session.key, fileId)
                        .map { response ->
                            val headers = response.headers()["X-Metadata"]
                            val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                            val payloadHeader =
                                Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                            val result: ByteArray =
                                response.body()?.byteStream()?.readBytes() as ByteArray

                            val contentBytes: ByteArray =
                                DataDecryptor.dataFromEncryptedBytes(
                                    result,
                                    configuration.privateKeyHex
                                )

                            val compression: String = try {
                                payloadHeader.compression
                            } catch (e: Throwable) {
                                Compressor.COMPRESSION_NONE
                            }
                            val decompressedContentBytes: ByteArray =
                                Compressor.decompressData(contentBytes, compression)

                            FileItem().copy(
                                fileContent = String(decompressedContentBytes),
                                fileName = fileId
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { completion.invoke(it, null) },
                            onError = {
                                completion.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        it.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    completion.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleGetSessionData(
        userAccessToken: String,
        fileId: String,
        completion: FileContentCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                userAccessToken
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .triggerDataQuery(authHeader, Pull())
            ) { response: DataQueryResponse?, error: Error? ->
                when {
                    response != null -> {
                        sessionManager.updatedSession = response.session
                        emitter.onSuccess(response)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        requestDataQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session

                    apiClient.argonService.getFileBytes(it.session.key, fileId)
                        .map { response ->
                            val headers = response.headers()["X-Metadata"]
                            val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                            val payloadHeader =
                                Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                            val result: ByteArray =
                                response.body()?.byteStream()?.readBytes() as ByteArray

                            val contentBytes: ByteArray =
                                DataDecryptor.dataFromEncryptedBytes(
                                    result,
                                    configuration.privateKeyHex
                                )

                            val compression: String = try {
                                payloadHeader.compression
                            } catch (e: Throwable) {
                                Compressor.COMPRESSION_NONE
                            }
                            val decompressedContentBytes: ByteArray =
                                Compressor.decompressData(contentBytes, compression)

                            FileItem().copy(
                                fileContent = String(decompressedContentBytes),
                                fileName = fileId
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { completion.invoke(it, null) },
                            onError = {
                                completion.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        it.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    completion.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleCyclicDataDownload(
        scope: DataRequest?, userAccessToken: String,
        downloadHandler: FileContentCompletion,
        completion: FileListCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                userAccessToken
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            val dataQueryScope: Pull = scope?.let { scope -> Pull(scope) } ?: Pull()

            apiClient.makeCall(
                apiClient
                    .argonService
                    .triggerDataQuery(authHeader, dataQueryScope)
            ) { response: DataQueryResponse?, error: Error? ->
                when {
                    response != null -> {
                        sessionManager.updatedSession = response.session
                        emitter.onSuccess(response)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }

        requestDataQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    sessionManager.updatedSession = it.session
                    handleContinuousDataDownload(userAccessToken, downloadHandler, completion)
                },
                onError = {
                    completion.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleContinuousDataDownload(
        userAccessToken: String,
        downloadHandler: FileContentCompletion,
        completion: FileListCompletion
    ) {
        DMELog.i(context.getString(R.string.labelReadingFiles))

        activeFileDownloadHandler = downloadHandler
        activeSessionDataFetchCompletionHandler = completion

        getSessionFileList(userAccessToken, { _, updatedFileIds ->

            updatedFileIds.forEach {

                activeDownloadCount++
                DMELog.i("Downloading file with ID: $it.")

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
            if (fileList?.syncStatus == FileList.SyncStatus.COMPLETED() && error == null && activeDownloadCount == 0) {
                completion(
                    fileList,
                    null
                ) // We only want to push this if the error exists, else
                // it'll cause a premature loop exit.
            } else if (fileList?.syncStatus == FileList.SyncStatus.PARTIAL()) {
                completion(
                    fileList,
                    error
                )
            }
        }
    }
}