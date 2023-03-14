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
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.OnboardPayload
import me.digi.sdk.entities.payload.PreAuthorizationCodePayload
import me.digi.sdk.entities.payload.TokenReferencePayload
import me.digi.sdk.entities.request.*
import me.digi.sdk.entities.response.*
import me.digi.sdk.interapp.managers.SaasConsentManager
import me.digi.sdk.utilities.*
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.FileListItemCache
import me.digi.sdk.utilities.crypto.*
import me.digi.sdk.utilities.jwt.*
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

    private val reAuthConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(configuration.baseUrl, context.getString(R.string.labelSaasReAuth))
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

    var sessionKey: String? = ""
    var sdkVersion: String = BuildConfig.SDK_VERSION
    var syncRunning: Boolean = false

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
                    null,
                    scope,
                    serviceId
                )!!
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: GetTokenExchangeDone ->
                    sessionManager.updatedSession = result.consentData.session
                    sessionKey = sessionManager.updatedSession?.key
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

    fun reAuthorizeAccount(
        fromActivity: Activity,
        accountId: String?,
        credentials: CredentialsPayload,
        completion: GetOnboardDoneCompletion
    ) {

        fun requestAccountIdReference(): SingleTransformer<OnboardPayload, AccountIdReferencePayload> =
            SingleTransformer {
                it.flatMap { referenceCodePayload ->
                    Single.create { emitter ->
                        DMELog.i(context.getString(R.string.labelReferenceOnboardingCode))

                        val jwt = AccountIdReferenceRequestJWT(
                            configuration.appId,
                            configuration.contractId,
                        )

                        val signingKey: PrivateKey =
                            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                        val authHeader: String = jwt.sign(signingKey).tokenize()

                        val accountIdRequest = AccountIdRequest()
                        accountIdRequest.value = accountId

                        apiClient.makeCall(
                            apiClient.argonService.getAccountIdReference(
                                authHeader,
                                accountIdRequest
                            )
                        ) { response, error ->
                            when {
                                response != null -> {
                                    val accountIdReferencePayload =
                                        AccountIdReferencePayload(
                                            tokenReferencePayload = referenceCodePayload.tokenReferencePayload,
                                            session = referenceCodePayload.session,
                                            code = referenceCodePayload.code,
                                            accountId = response.id
                                        )
                                    emitter.onSuccess(accountIdReferencePayload)
                                }
                                error != null -> emitter.onError(error)
                                else -> emitter.onError(IllegalArgumentException())
                            }
                        }
                    }
                }
            }

        fun requestReAuth(): SingleTransformer<AccountIdReferencePayload, GetOnboardDone> =
            SingleTransformer {
                it.flatMap { accountIdReferencePayload ->
                    Single.create { emitter ->
                        DMELog.i(context.getString(R.string.labelUserOnboardingRequest))

                        accountIdReferencePayload.tokenReferencePayload?.referenceCode?.let { code ->
                            reAuthConsentManager.beginReAuthAction(
                                fromActivity,
                                code,
                                accountIdReferencePayload.accountId
                            ) { onboardResponse: OnboardAuthResponse?, error: Error? ->
                                when {
                                    onboardResponse?.success == true -> {
                                        val consentDone = GetOnboardDone()
                                            .copy(
                                                session = accountIdReferencePayload.session,
                                                onboardResponse = onboardResponse,
                                                credentials = credentials
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


        requestCodeReference(credentials)
            .compose(requestAccountIdReference())
            .compose(requestReAuth())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {error ->
                completion.invoke(
                    null,
                    error.let { it as? Error }
                        ?: APIError.ErrorWithMessage(
                            error.localizedMessage ?: "Unknown error occurred"
                        ))
            }, onSuccess = {
                    result ->
                sessionKey = sessionManager.updatedSession?.key
                isFirstRun = true

                val response = OnboardResponse().copy(
                    session = sessionManager.updatedSession,
                    onboardResponse = result.onboardResponse,
                    credentials = result.credentials
                )

                completion.invoke(response, null)
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
        credentials: CredentialsPayload,
        completion: UserDeleteCompletion
    ) {
        DMELog.i(context.getString(R.string.labelDeleteLibrary))

        fun deleteLibrary() = Single.create<Boolean> { emitter ->
            credentials.accessToken.value?.let { accessToken ->

                val jwt = UserDeletionRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    accessToken
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
    fun readAccounts(credentials: CredentialsPayload, completion: AccountsCompletion) {
        DMELog.i(context.getString(R.string.labelReadAccounts))

        val currentSession = sessionManager.updatedSession

        val jwt = PermissionAccessRequestJWT(
            credentials.accessToken.value!!,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()

        if (isFirstRun and (currentSession != null && sessionManager.isSessionValid())) {
            apiClient.argonService.getFileBytes(currentSession?.key!!, "accounts.json")
                .map { response: Response<ResponseBody> ->

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
        } else handleReadAccounts(credentials, completion)
    }

    /**
     * Writes data to user's library associated with configured contract
     *
     * @param writeDataPayload The data to be written
     * @param credentials to reference the existing library
     * @param completion Block called on completion with updated/returned session values with
     * delivery status or any error encountered.
     */
    fun write(
        credentials: CredentialsPayload,
        writeDataPayload: WriteDataPayload,
        userAccessToken: String,
        completion: OngoingWriteCompletion
    ) {
        DMELog.i(context.getString(R.string.labelWriteDataToLibrary))

        if (sessionManager.isSessionValid())
            handleDataWrite(credentials, writeDataPayload, completion)
        else
            updateSession { _, error ->
                error?.let {
                    DMELog.e("Your session is invalid; please request a new one.")
                    completion(null, AuthError.InvalidSession())
                }
                    ?: run {
                        handleDataWrite(credentials, writeDataPayload, completion)
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
            .doOnSuccess {
                val session = Session().copy(key = it.key, expiry = it.expiry)
                sessionManager.updatedSession = session
                sessionKey = sessionManager.updatedSession?.key

                completion.invoke(true, null)
            }
            .doOnError {
                completion.invoke(
                    false,
                    AuthError.ErrorWithMessage(
                        it.localizedMessage ?: "Unknown error occurred"
                    )
                )
            }
            .subscribe()
    }

    private fun requestCodeReference(refreshedCredentialsPayload: CredentialsPayload): Single<OnboardPayload> =
        Single.create { emitter ->
            DMELog.i(context.getString(R.string.labelReferenceOnboardingCode))

            val jwt = ReferenceCodeRequestJWT(
                configuration.appId,
                configuration.contractId,
                refreshedCredentialsPayload.accessToken.value!!
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.getReferenceCode(authHeader)) { tokenReference, error ->
                when {
                    tokenReference != null -> {
                        val chunks: List<String> = tokenReference.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))

                        sessionManager.updatedSession = tokenReference.session

                        val tokenReferencePayload: TokenReferencePayload =
                            Gson().fromJson(payloadJson, TokenReferencePayload::class.java)

                        val result = OnboardPayload(tokenReferencePayload, tokenReference.session)

                        emitter.onSuccess(result)
                    }
                    error != null -> emitter.onError(error)
                    else -> emitter.onError(IllegalArgumentException())
                }
            }
        }


    /**
     * Once a user has granted consent, adds an additional service
     *
     * @param serviceId Identifier of service to add
     * @param credentials to reference the same library
     * @param completion Block called upon completion with any errors encountered
     */
    fun addService(
        fromActivity: Activity,
        serviceId: String,
        scope: DataRequest?,
        credentials: CredentialsPayload,
        completion: GetOnboardDoneCompletion
    ) {

        var refreshedCredentialsPayload = credentials

        fun requestOnboard(): SingleTransformer<OnboardPayload, GetOnboardDone> =
            SingleTransformer {
                it.flatMap { onboardPayload ->
                    Single.create { emitter ->
                        DMELog.i(context.getString(R.string.labelUserOnboardingRequest))

                        onboardPayload.tokenReferencePayload?.referenceCode?.let { code ->
                            onboardConsentManager.beginOnboardAction(
                                fromActivity,
                                code,
                                serviceId
                            ) { onboardResponse: OnboardAuthResponse?, error: Error? ->
                                when {
                                    onboardResponse?.success == true -> {
                                        val consentDone = GetOnboardDone()
                                            .copy(
                                                session = onboardPayload.session,
                                                onboardResponse = onboardResponse,
                                                credentials = refreshedCredentialsPayload
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

        if (credentials.accessToken.expiresOn.isValid())
            requestCodeReference(refreshedCredentialsPayload)
                .compose(requestOnboard())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { result ->
                    sessionKey = sessionManager.updatedSession?.key
                    isFirstRun = true

                    val response = OnboardResponse().copy(
                        session = sessionManager.updatedSession,
                        onboardResponse = result.onboardResponse,
                        credentials = result.credentials
                    )

                    completion.invoke(response, null)
                }
                .doOnError { error ->
                    completion.invoke(
                        null,
                        error.let { it as? Error }
                            ?: APIError.ErrorWithMessage(
                                error.localizedMessage ?: "Unknown error occurred"
                            ))
                }
                .subscribe()
        else {
            refreshCredentials(
                fromActivity,
                credentials,
                scope,
                serviceId
            ) { refreshResponse, _ ->
                refreshedCredentialsPayload = refreshResponse?.credentials!!

                requestCodeReference(refreshedCredentialsPayload)
                    .compose(requestOnboard())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess { result ->
                        sessionKey = sessionManager.updatedSession?.key
                        isFirstRun = true

                        val response = OnboardResponse().copy(
                            session = sessionManager.updatedSession,
                            onboardResponse = result.onboardResponse,
                            credentials = result.credentials
                        )

                        completion.invoke(response, null)
                    }
                    .doOnError { addServiceError ->
                        completion.invoke(
                            null,
                            addServiceError.let { it as? Error }
                                ?: APIError.ErrorWithMessage(
                                    addServiceError.localizedMessage ?: "Unknown error occurred"
                                ))
                    }
                    .subscribe()
            }
        }
    }

    /**
     * Get a list of possible services a user can add to their digi.me library
     *
     * @param completion Block called upon completion with either the service list
     * or any errors encountered
     */
    fun getAvailableServices(
        contractId: String,
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
        credentials: CredentialsPayload,
        downloadHandler: FileContentCompletion,
        completion: FileListCompletion
    ) {

        val currentSession = sessionManager.updatedSession
        syncRunning = true

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
    fun readFileList(credentials: CredentialsPayload, completion: FileListCompletion) {

        val currentSession = sessionManager.updatedSession

        if ((currentSession != null && sessionManager.isSessionValid()) and (activeSyncStatus != FileList.SyncStatus.COMPLETED() && activeSyncStatus != FileList.SyncStatus.PARTIAL())) {
            apiClient.makeCall(
                apiClient.argonService.getFileList(currentSession?.key!!),
                completion
            )
        } else handleFileList(credentials, completion)
    }

    /**
     * Get a file content by file ID.
     *
     * @param fileId ID for specific file
     * @param completion Block called upon completion with either file or any errors encountered.
     */
    fun readFile(
        credentials: CredentialsPayload,
        fileId: String,
        completion: FileContentBytesCompletion
    ) {

        val currentSession = sessionManager.updatedSession

        val jwt = PermissionAccessRequestJWT(
            credentials.accessToken.value!!,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()

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
        } else handleFileItemBytes(credentials, fileId, completion)
    }

    private fun getSessionData(fileId: String, completion: FileContentCompletion) {
        val currentSession = sessionManager.updatedSession

        val jwt = PermissionAccessRequestJWT(
            credentials.accessToken.value!!,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()

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
        } else handleGetSessionData(credentials, fileId, completion)
    }

    private fun getSessionFileList(
        credentials: CredentialsPayload,
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

        if (activeSyncStatus == null) {
            // Init syncStatus.
            fileListItemCache = FileListItemCache()
            scheduleNextPoll(credentials)
        }
    }

    private fun scheduleNextPoll(credentials: CredentialsPayload) {

        DMELog.d("Session data poll scheduled.")

        DMELog.d("Fetching file list.")
        if (activeSyncStatus != FileList.SyncStatus.COMPLETED()) {
            Handler(Looper.getMainLooper()).postDelayed({
                readFileList(credentials) { fileList, listFetchError ->

                    when {
                        fileList != null -> DMELog.d("File list obtained; Sync syncStatus is ${fileList.syncStatus.rawValue}.")
                        listFetchError != null -> DMELog.d("Error fetching file list: ${listFetchError.message}.")
                    }

                    val syncStatus = fileList?.syncStatus ?: FileList.SyncStatus.RUNNING()
                    when (fileList?.syncStatus) {
                        FileList.SyncStatus.RUNNING() -> {
                            DMELog.d("Sync status: running")
                        }

                        FileList.SyncStatus.COMPLETED() -> {
                            DMELog.d("Sync status: completed")
                        }

                        FileList.SyncStatus.PENDING() -> {
                            DMELog.d("Sync status: pending")
                        }

                        FileList.SyncStatus.PARTIAL() -> {
                            DMELog.d("Sync status: partial")
                        }
                    }


                    latestFileList = fileList
                    val updatedFileIds = fileListItemCache?.updateCacheWithItemsAndDeduceChanges(
                        fileList?.fileList.orEmpty()
                    ).orEmpty()

                    DMELog.i(
                        "${
                            fileList?.fileList.orEmpty().count()
                        } files discovered. Of these, ${updatedFileIds.count()} have updates and need downloading."
                    )

                    if (updatedFileIds.isNotEmpty() && fileList != null) {
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
                            scheduleNextPoll(credentials)
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
        }
    }

    private fun refreshAccessToken(
        credentials: CredentialsPayload,
        scope: DataRequest?,
        completion: RefreshCompletion
    ) {
        requestPreAuthorizationCode(credentials, scope)
            .map { response: GetPreAuthCodeDone ->
                GetTokenExchangeDone()
                    .copy(
                        consentData = GetConsentDone(session = response.session),
                        credentials = credentials
                    )
            }
            .compose(requestCredentialsRefresh())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: GetTokenExchangeDone ->
                    sessionKey = sessionManager.updatedSession?.key
                    isFirstRun = true

                    val response = GetTokenExchangeDone().copy(
                        consentData = result.consentData,
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

    private fun obtainNewRefreshToken(
        fromActivity: Activity,
        credentials: CredentialsPayload,
        serviceId: String?,
        scope: DataRequest?,
        completion: RefreshCompletion
    ) {
        requestPreAuthorizationCode(credentials, scope)
            .compose(requestConsentAccess(fromActivity, serviceId))
            .compose(requestTokenExchange())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: GetTokenExchangeDone ->
                    sessionKey = sessionManager.updatedSession?.key
                    isFirstRun = true

                    val response = GetTokenExchangeDone().copy(
                        consentData = result.consentData,
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

    private fun refreshCredentials(
        fromActivity: Activity,
        credentials: CredentialsPayload,
        scope: DataRequest?,
        serviceId: String?,
        completion: RefreshCompletion
    ) {
        if (credentials.refreshToken.expiresOn.isValid()) {
            refreshAccessToken(credentials, scope, completion)
        } else {
            obtainNewRefreshToken(fromActivity, credentials, serviceId, scope, completion)
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
                .onErrorResumeNext { requestCredentialsError ->

                    // If an error is encountered from this call, we inspect it to see if it's an
                    // 'InvalidToken' error, meaning that the REFRESH token has expired.
                    if (requestCredentialsError is APIError && error.code == "InvalidToken") {
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
        } else Single.error(error!!)
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

//    private fun requestReAuth(
//        fromActivity: Activity,
//        accountId: String?
//    ): SingleTransformer<in GetPreAuthCodeDone, out GetConsentDone> =
//        SingleTransformer<GetPreAuthCodeDone, GetConsentDone> {
//            it.flatMap { input: GetPreAuthCodeDone ->
//                Single.create { emitter ->
//                    input.payload.preAuthorizationCode?.let { code ->
//                        reAuthConsentManager.beginConsentAction(
//                            fromActivity,
//                            code,
//                            accountId
//                        ) { consentResponse: ConsentAuthResponse?, error: Error? ->
//                            when {
//                                consentResponse?.success == true -> {
//                                    val consentDone = GetConsentDone()
//                                        .copy(
//                                            session = input.session,
//                                            consentResponse = consentResponse
//                                        )
//
//                                    emitter.onSuccess(consentDone)
//                                }
//                                error != null -> emitter.onError(error)
//                                else -> emitter.onError(IllegalArgumentException())
//                            }
//
//                        }
//                    }
//                }
//            }
//        }

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
                    sessionKey = sessionManager.updatedSession?.key

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
    private fun handleReadAccounts(
        credentials: CredentialsPayload,
        completion: AccountsCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                credentials.accessToken.value!!
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
                        sessionKey = sessionManager.updatedSession?.key

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
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        credentials.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.argonService.getFileBytes(authHeader, it.session.key, "accounts.json")
                        .map { response: Response<ResponseBody> ->

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

                            Gson().fromJson(
                                String(decompressedContentBytes),
                                ReadAccountsResponse::class.java
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { response ->
                                completion.invoke(response, null)
                            },
                            onError = { throwable ->
                                completion.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        throwable.localizedMessage ?: "Unknown error occurred"
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
        credentials: CredentialsPayload,
        writeDataPayload: WriteDataPayload,
        userAccessToken: String,
        completion: OngoingWriteCompletion
    ) {
        val requestBody: RequestBody =
            writeDataPayload.content.toRequestBody(
                writeDataPayload.metadata.mimeType?.toMediaTypeOrNull(),
                0,
                writeDataPayload.content.size
            )

        val jwt = DirectImportRequestJWT(
            credentials.accessToken.value!!,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()

        apiClient.argonService.pushOngoingData(
            authHeader,
            writeDataPayload.data.key!!,
            encryptedData.symmetricalKey,
            encryptedData.iv,
            encryptedData.metadata,
            writeDataPayload.data.postboxId!!,
            multipartBody.requestBody,
            multipartBody.description
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    sessionManager.updatedSession = it.session
                    completion(
                        it,
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

//    {"access_token":{"expires_on":1664831686,"value":"d8e19799c75ed9db2f2eca9f1d9062fdcd549adc4fcb7538449300654ff742a3a3c5901562a67fb8136c25b7cbc906cb94866840fccfbc1074676f9f6c84a1fba77a6bd90f07465cb6b6fa89650da325"},"consentid":"df2f16318deb7d85672ec1f1e0ce652b","identifier":{"id":"3111d038abcf6e58868c4b8fcd26849b"},"refresh_token":{"expires_on":1680297286,"value":"0d54664b62de08472d1db7348638aecf0c8e266cff37278dc42718bcbe7a5cb1de870a86a4e74ffaa2f2f40228810e53be42d0f8b0dc57bb58471b567fb9a99f7b7e2318cb17a53b976a9c96cea294cd"},"token_type":"Bearer"}
    data class UserAccessToken(
        val accessToken: AccessToken? = null,
    )

    data class AccessToken (
        val expires_on: Long = 0L,
        val value: String? = null
    )



    private fun handleFileList(
        credentials: CredentialsPayload,
        completion: FileListCompletion
    ) {
        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->

            val accessToken = Gson().fromJson<UserAccessToken>(userAccessToken, UserAccessToken::class.java)
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                accessToken.accessToken?.value!!)

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
                        sessionKey = sessionManager.updatedSession?.key

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
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        credentials.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

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
        credentials: CredentialsPayload,
        fileId: String,
        completion: FileContentBytesCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                credentials.accessToken.value!!
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
                        sessionKey = sessionManager.updatedSession?.key

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
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        credentials.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.argonService.getFileBytes(authHeader, it.session.key, fileId)
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
                            onSuccess = { result ->
                                completion.invoke(result, null)
                            },
                            onError = { throwable ->
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
        credentials: CredentialsPayload,
        fileId: String,
        completion: FileContentCompletion
    ) {

        fun requestDataQuery(): Single<out DataQueryResponse> = Single.create { emitter ->
            val jwt = TriggerDataQueryRequestJWT(
                configuration.appId,
                configuration.contractId,
                credentials.accessToken.value!!
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
                        sessionKey = sessionManager.updatedSession?.key

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
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        credentials.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.argonService.getFileBytes(authHeader, it.session.key, fileId)
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
                            onSuccess = { result ->
                                completion.invoke(result, null)
                            },
                            onError = { throwable ->
                                completion.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        throwable.localizedMessage ?: "Unknown error occurred"
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
                credentials.accessToken.value!!
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
                        sessionKey = sessionManager.updatedSession?.key

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

                    sessionKey = sessionManager.updatedSession?.key

                    handleContinuousDataDownload(credentials, downloadHandler, completion)
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
        credentials: CredentialsPayload,
        downloadHandler: FileContentCompletion,
        completion: FileListCompletion
    ) {
        DMELog.i(context.getString(R.string.labelReadingFiles))

        activeFileDownloadHandler = downloadHandler
        activeSessionDataFetchCompletionHandler = completion

        getSessionFileList(credentials, { _, updatedFileIds ->

            updatedFileIds.forEach {

                activeDownloadCount++
                DMELog.i("Downloading file with ID: $it.")


                getSessionData(it, credentials) { file, error ->

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
            } else if (fileList?.accounts?.hasError() == true) {
                val reAuthError = APIError.REAUTHREQUIRED()
                reAuthError.accountIds = fileList.accounts.errorAccounts()
                syncRunning = false
                completion(
                    fileList,
                    reAuthError
                )
            } else if (fileList?.syncStatus == FileList.SyncStatus.PARTIAL()) {
                syncRunning = false
                completion(
                    fileList,
                    error
                )
            }
        }
    }
}