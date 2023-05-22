package me.digi.sdk

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.security.PrivateKey
import kotlin.math.max

class Init(
    val context: Context,
    val configuration: DigiMeConfiguration,
    //val saveTokens: (tokens: CredentialsPayload) -> Unit,
    //val getTokens: () -> CredentialsPayload
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

    private var activeFileDownloadHandler: FileContentCallback? = null
    private var activeSessionDataFetchCallbackHandler: FileListCallback? = null
    private var fileListUpdateHandler: IncrementalFileListUpdate? = null
    private var fileListCallbackHandler: FileListCallback? = null
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
                    FileList.SyncStatus.PARTIAL() -> completeDeliveryOfSessionData(
                        null
                    )
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
                    FileList.SyncStatus.PARTIAL() -> completeDeliveryOfSessionData(
                        null
                    )
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

    private fun checkAccessToken(
        credentials: CredentialsPayload
    ): Single<out CredentialsPayload> {
        return if (!credentials.accessToken.expiresOn.isValid())
            requestNewTokens(credentials)
        else
            Single.just(credentials)
    }

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
     * @param callback Block called upon authorization with any errors encountered.
     */
    fun authorizeAccess(
        fromActivity: Activity,
        scope: DataRequest? = null,
        credentials: CredentialsPayload? = null,
        serviceId: String? = null,
        callback: AuthorizeAccessCallback
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
                onSuccess = { result: TokenExchangeResponse ->
                    sessionManager.updatedSession = result.consentData.session
                    sessionKey = sessionManager.updatedSession?.key
                    isFirstRun = true

                    val response = AuthorizationResponse().copy(
                        session = result.consentData.session,
                        authResponse = result.consentData.consentResponse,
                        credentials = result.credentials
                    )

                    callback.invoke(response, null)
                },
                onError = { error ->
                    callback.invoke(
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
        callback: AuthorizeAccessCallback
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

        fun requestReAuth(): SingleTransformer<AccountIdReferencePayload, AuthorizationResponse> =
            SingleTransformer {
                it.flatMap { accountIdReferencePayload ->
                    Single.create { emitter ->
                        DMELog.i(context.getString(R.string.labelUserOnboardingRequest))

                        accountIdReferencePayload.tokenReferencePayload?.referenceCode?.let { code ->
                            reAuthConsentManager.beginReAuthAction(
                                fromActivity,
                                code,
                                accountIdReferencePayload.accountId
                            ) { onboardResponse: SaasCallbackResponse?, error: Error? ->
                                when {
                                    onboardResponse?.success == true -> {
                                        val consentDone = AuthorizationResponse()
                                            .copy(
                                                session = accountIdReferencePayload.session,
                                                authResponse = onboardResponse,
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

        checkAccessToken(credentials)
            .compose(requestCodeReference())
            .compose(requestAccountIdReference())
            .compose(requestReAuth())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = { error ->
                callback.invoke(
                    null,
                    error.let { it as? Error }
                        ?: APIError.ErrorWithMessage(
                            error.localizedMessage ?: "Unknown error occurred"
                        ))
            }, onSuccess = { result ->
                sessionKey = sessionManager.updatedSession?.key
                isFirstRun = true

                callback.invoke(result, null)
            })
    }

    /**
     * Deletes the user's library associated with the configured contract.
     *
     * Please note that if multiple contracts are linked to the same library,
     * the n 'deleteUser' will also need to be called on those contracts to remove
     * any stored credentials, in which case an error may be reported on those calls.
     *
     * @param callback Block called on completion with value true/false upon library deletion
     * or any error encountered
     */
    fun deleteUser(
        credentials: CredentialsPayload,
        callback: DeleteUserCallback
    ) {
        DMELog.i(context.getString(R.string.labelDeleteLibrary))

        fun deleteLibrary(): SingleTransformer<CredentialsPayload, DeleteUserResponse> =
            SingleTransformer { credentialsPayload ->
                credentialsPayload.flatMap { credentials ->
                    Single.create<DeleteUserResponse> { emitter ->
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
                                    else -> emitter.onSuccess(DeleteUserResponse(true, credentials))
                                }
                            }
                        }
                            ?: emitter.onError(APIError.ErrorWithMessage(context.getString(R.string.labelAccessTokenInvalidOrMissing)))
                    }
                }
            }

        checkAccessToken(credentials)
            .compose(deleteLibrary())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = {
                callback.invoke(
                    it,
                    null
                )
            }, onError = {
                it.localizedMessage?.let { message ->
                    if (message.contains("204"))
                        callback.invoke(DeleteUserResponse(true, credentials), null)
                    else callback.invoke(
                        null,
                        APIError.ErrorWithMessage(message)
                    )
                }
            })
    }

    /**
     * Gets list of possible accounts from the users library.
     *
     * @param callback Block called upon completion with either list of accounts in the library;
     * or any errors encountered.
     */
    fun readAccounts(credentials: CredentialsPayload, callback: GetAccountsCallback) {
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
            getAccountsContent(callback, currentSession?.key!!, authHeader, credentials)
        } else handleReadAccounts(credentials, callback)
    }

    /**
     * Writes data to user's library associated with configured contract
     *
     * @param writeDataPayload The data to be written
     * @param credentials to reference the existing library
     * @param callback Block called on completion with updated/returned session values with
     * delivery status or any error encountered.
     */
    fun write(
        credentials: CredentialsPayload,
        writeDataPayload: WriteDataPayload,
        callback: WriteDataCallback
    ) {
        DMELog.i(context.getString(R.string.labelWriteDataToLibrary))

        if (sessionManager.isSessionValid())
            handleDataWrite(credentials, writeDataPayload, callback)
        else
            updateSession { _, error ->
                error?.let {
                    DMELog.e("Your session is invalid; please request a new one.")
                    callback(WriteDataResponse(null, credentials), AuthError.InvalidSession())
                }
                    ?: run {
                        handleDataWrite(credentials, writeDataPayload, callback)
                    }
            }
    }

    /**
     * Once a user has granted consent, adds an additional service
     *
     * @param serviceId Identifier of service to add
     * @param credentials to reference the same library
     * @param callback Block called upon completion with any errors encountered
     */
    fun addService(
        fromActivity: Activity,
        serviceId: String,
        scope: DataRequest?,
        credentials: CredentialsPayload,
        callback: AuthorizeAccessCallback
    ) {

        fun requestOnboard(): SingleTransformer<OnboardPayload, AuthorizationResponse> =
            SingleTransformer {
                it.flatMap { onboardPayload ->
                    Single.create { emitter ->
                        DMELog.i(context.getString(R.string.labelUserOnboardingRequest))

                        onboardPayload.tokenReferencePayload?.referenceCode?.let { code ->
                            onboardConsentManager.beginOnboardAction(
                                fromActivity,
                                code,
                                serviceId
                            ) { onboardResponse: SaasCallbackResponse?, error: Error? ->
                                when {
                                    onboardResponse?.success == true -> {
                                        val consentDone = AuthorizationResponse()
                                            .copy(
                                                session = onboardPayload.session,
                                                authResponse = onboardResponse,
                                                credentials = onboardPayload.credentials
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

        checkAccessToken(credentials)
            .compose(requestCodeReference())
            .compose(requestOnboard())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                callback.invoke(response, null)
            }, { error ->
                callback.invoke(
                    null,
                    error.let { it as? Error }
                        ?: APIError.ErrorWithMessage(
                            error.localizedMessage ?: "Unknown error occurred"
                        ))
            }
            )
    }

    /**
     * Get a list of possible services a user can add to their digi.me library
     *
     * @param callback Block called upon completion with either the service list
     * or any errors encountered
     */
    fun getAvailableServices(
        contractId: String,
        callback: AvailableServicesCallback
    ) {
        DMELog.i(context.getString(R.string.labelAvailableServices))

        apiClient.argonService.getServicesForContract(contractId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { callback.invoke(it, null) }, onError = {
                callback.invoke(
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
     * @param callback Block called when fetching all files has completed. Contains
     * final list of files or an error if reading file list failed.
     */
    fun readAllFiles(
        scope: DataRequest? = null,
        credentials: CredentialsPayload,
        credentialsCallback: CredentialsCallback,
        fileContentCallback: FileContentCallback,
        isOnboarding: Boolean,
        fileListCallback: FileListCallback
    ) {

        val currentSession = sessionManager.updatedSession
        syncRunning = true

        isFirstRun = isOnboarding
        if (isFirstRun and (currentSession != null && sessionManager.isSessionValid())) {
            checkAccessToken(credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    credentialsCallback.invoke(it, null)
                    handleContinuousDataDownload(it, fileContentCallback, fileListCallback)
                }, {
                    credentialsCallback.invoke(
                        null,
                        AuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                })
        } else {
            checkAccessToken(credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    credentialsCallback.invoke(it, null)
                    handleCyclicDataDownload(scope, it, fileContentCallback, fileListCallback)
                }, {
                    credentialsCallback.invoke(
                        null,
                        AuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                })
        }
    }

    /**
     * Get list of possible files from the users library.
     *
     * @param callback Block called upon completion with either list of files in the library;
     * returned as json objects, or any errors encountered.
     */
    fun readFileList(credentials: CredentialsPayload, callback: FileListCallback) {

        val currentSession = sessionManager.updatedSession

        val jwt = PermissionAccessRequestJWT(
            credentials.accessToken.value!!,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()

        if ((currentSession != null && sessionManager.isSessionValid()) and (activeSyncStatus != FileList.SyncStatus.COMPLETED() && activeSyncStatus != FileList.SyncStatus.PARTIAL())) {

            if (credentials.accessToken.expiresOn.isValid())
                apiClient.argonService.getFileList(authHeader, currentSession?.key!!)
                    .map {

                        it.credentials = credentials
                        it
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = { callback.invoke(it, null) },
                        onError = {
                            callback.invoke(
                                null,
                                AuthError.ErrorWithMessage(
                                    it.localizedMessage ?: "Unknown error occurred"
                                )
                            )
                        }
                    )
            else {
                refreshAccessToken(credentials, null) { tokenExchangeResponse, error ->
                    val newJwt = PermissionAccessRequestJWT(
                        tokenExchangeResponse?.credentials?.accessToken?.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val newSigningKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val newAuthHeader: String = newJwt.sign(newSigningKey).tokenize()

                    apiClient.argonService.getFileList(newAuthHeader, currentSession?.key!!)
                        .map {

                            it.credentials = tokenExchangeResponse.credentials
                            it
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { callback.invoke(it, null) },
                            onError = {
                                callback.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        it.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                }
            }
        } else handleFileList(credentials, callback)
    }

    /**
     * Get a file content by file ID.
     *
     * @param fileId ID for specific file
     * @param callback Block called upon completion with either file or any errors encountered.
     */
    fun readFile(
        credentials: CredentialsPayload,
        fileId: String,
        callback: FileContentBytesCallback
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
            apiClient.argonService.getFileBytes(authHeader, currentSession?.key!!, fileId)
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

                    FileItemBytes().copy(
                        fileContent = decompressedContentBytes,
                        credentials = credentials
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { callback.invoke(it, null) },
                    onError = {
                        callback.invoke(
                            null,
                            AuthError.ErrorWithMessage(
                                it.localizedMessage ?: "Unknown error occurred"
                            )
                        )
                    }
                )
        } else handleFileItemBytes(credentials, fileId, callback)
    }

    fun getPortabilityReport(
        credentials: CredentialsPayload,
        serviceType: String,
        format: String,
        from: String,
        to: String,
        callback: PortabilityReportCallback
    ){
        fun getPortabilityReport(): SingleTransformer<in CredentialsPayload, out PortabilityReportResponse> =
        SingleTransformer {
            it.flatMap { onboardPayload ->
                Single.create { emitter ->
                    val jwt = PermissionAccessRequestJWT(
                        onboardPayload.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.argonService.getPortabilityReport(authHeader, serviceType, format, from, to)
                        .map { response: Response<ResponseBody> ->
                            PortabilityReportResponse(
                                response.body()?.bytes(),
                                credentials
                            )

                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { result ->
                                callback.invoke(result, null)
                            },
                            onError = { throwable ->
                                callback.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        throwable.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                }
            }
        }

        checkAccessToken(credentials)
            .compose(getPortabilityReport())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result ->
                    callback.invoke(result, null)

                },
                onError = { throwable ->
                    callback.invoke(
                        null,
                        AuthError.ErrorWithMessage(
                            throwable.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
        val jwt = PermissionAccessRequestJWT(
            credentials.accessToken.value!!,
            configuration.appId,
            configuration.contractId
        )

        val signingKey: PrivateKey =
            KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
        val authHeader: String = jwt.sign(signingKey).tokenize()
    }

    /**
     * Requests new session, and updates session manager with it.
     *
     * @param callback Block called upon completion with updated session, boolean value
     * if session was in fact updated, or any error encountered.
     */
    private fun updateSession(callback: GetSessionCallback) {

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
            .subscribe({
                val session = Session().copy(key = it.key, expiry = it.expiry)
                sessionManager.updatedSession = session
                sessionKey = sessionManager.updatedSession?.key

                callback.invoke(true, null)
            }, {
                callback.invoke(
                    false,
                    AuthError.ErrorWithMessage(
                        it.localizedMessage ?: "Unknown error occurred"
                    )
                )
            })
    }

    private fun requestCodeReference(): SingleTransformer<CredentialsPayload, OnboardPayload> =
        SingleTransformer {
            it.flatMap { credentials ->
                Single.create { emitter ->
                    DMELog.i(context.getString(R.string.labelReferenceOnboardingCode))

                    val jwt = ReferenceCodeRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        credentials.accessToken.value!!
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

                                val result =
                                    OnboardPayload(
                                        tokenReferencePayload,
                                        tokenReference.session,
                                        null,
                                        credentials
                                    )

                                emitter.onSuccess(result)
                            }
                            error != null -> emitter.onError(error)
                            else -> emitter.onError(IllegalArgumentException())
                        }
                    }
                }
            }
        }

    private fun getSessionData(
        fileId: String,
        credentials: CredentialsPayload,
        callback: FileContentCallback
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
            apiClient.argonService.getFileBytes(authHeader, currentSession?.key!!, fileId)
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
                        callback.invoke(fileItem, null)
                    },
                    onError = {
                        callback.invoke(
                            null,
                            AuthError.ErrorWithMessage(
                                it.localizedMessage ?: "Unknown error occurred"
                            )
                        )
                    }
                )
        } else handleGetSessionData(credentials, fileId, callback)
    }

    private fun getSessionFileList(
        credentials: CredentialsPayload,
        updateHandler: IncrementalFileListUpdate,
        callback: FileListCallback
    ) {

        fileListUpdateHandler = updateHandler
        fileListCallbackHandler = { fileList, error ->
            val err = if (error is SDKError.FileListPollingTimeout) null else error
            callback(fileList, err)
            if (activeFileDownloadHandler == null && activeSessionDataFetchCallbackHandler == null) {
                completeDeliveryOfSessionData(err)
            }
        }

        if (activeSyncStatus == null || activeSyncStatus == FileList.SyncStatus.COMPLETED() || activeSyncStatus == FileList.SyncStatus.PARTIAL()) {
            // Init syncStatus.
            activeSyncStatus = null
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
                    fileList?.credentials = credentials

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
                        fileList?.credentials = credentials
                        fileListCallbackHandler?.invoke(
                            fileList,
                            SDKError.FileListPollingTimeout()
                        )
                        return@readFileList
                    }

                    when (syncStatus) {
                        FileList.SyncStatus.PENDING(),
                        FileList.SyncStatus.RUNNING() -> {
                            DMELog.i("Sync still in progress, continuing to poll for updates.")
                            scheduleNextPoll(credentials!!)
                        }
                        FileList.SyncStatus.COMPLETED(),
                        FileList.SyncStatus.PARTIAL() -> fileListCallbackHandler?.invoke(
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

    private fun refreshAccessToken(
        credentials: CredentialsPayload,
        scope: DataRequest?,
        callback: RefreshTokenCallback
    ) {
        requestPreAuthorizationCode(credentials, scope)
            .map { response: GetPreAuthCodeDone ->
                TokenExchangeResponse()
                    .copy(
                        consentData = GetConsentDone(session = response.session),
                        credentials = credentials
                    )
            }
            .compose(requestCredentialsRefresh())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: TokenExchangeResponse ->
                    sessionKey = sessionManager.updatedSession?.key
                    isFirstRun = true

                    val response = TokenExchangeResponse().copy(
                        consentData = result.consentData,
                        credentials = result.credentials
                    )

                    callback.invoke(response, null)
                },
                onError = { error ->
                    callback.invoke(
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
        callback: RefreshTokenCallback
    ) {
        requestPreAuthorizationCode(credentials, scope)
            .compose(requestConsentAccess(fromActivity, serviceId))
            .compose(requestTokenExchange())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: TokenExchangeResponse ->
                    sessionKey = sessionManager.updatedSession?.key
                    isFirstRun = true

                    val response = TokenExchangeResponse().copy(
                        consentData = result.consentData,
                        credentials = result.credentials
                    )

                    callback.invoke(response, null)
                },
                onError = { error ->
                    callback.invoke(
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
        callback: RefreshTokenCallback
    ) {
        if (credentials.refreshToken.expiresOn.isValid()) {
            refreshAccessToken(credentials, scope, callback)
        } else {
            obtainNewRefreshToken(fromActivity, credentials, serviceId, scope, callback)
        }
    }

    private fun completeDeliveryOfSessionData(error: Error?) {

        when {
            error != null -> DMELog.e("An error occurred whilst fetching session data. Error: ${error.message}")
            else -> DMELog.i("Session data fetch completed successfully.")
        }

        activeSessionDataFetchCallbackHandler?.invoke(latestFileList, error)

        // Clear syncStatus.
        fileListItemCache = null
        latestFileList = null
        activeFileDownloadHandler = null
        activeSessionDataFetchCallbackHandler = null
        activeSyncStatus = null
        activeDownloadCount = 0
    }

    private fun errorHandler(
        fromActivity: Activity,
        error: Throwable?,
        credentials: CredentialsPayload?,
        scope: DataRequest?,
        serviceId: String?
    ): SingleSource<out TokenExchangeResponse>? {

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
                    TokenExchangeResponse()
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
//                                .compose(requestDataQuery(scope))
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
                        ) { consentResponse: SaasCallbackResponse?, error: Error? ->
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
    private fun requestTokenExchange(): SingleTransformer<in GetConsentDone, out TokenExchangeResponse> =
        SingleTransformer<GetConsentDone, TokenExchangeResponse> {
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
                    TokenExchangeResponse().copy(
                        consentData = input,
                        credentials = credentialsPayload
                    )
                }
            }
        }

    private fun requestNewTokens(credentials: CredentialsPayload): Single<out CredentialsPayload> =
        Single.create { emitter ->
            val jwt = RefreshCredentialsRequestJWT(
                configuration.appId,
                configuration.contractId,
                credentials.refreshToken.value!!
            )

            val signingKey: PrivateKey =
                KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.makeCall(apiClient.argonService.refreshCredentials(authHeader))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { response ->
                        val chunks: List<String> = response.token.split(".")
                        val payloadJson = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                        val credentialsPayload =
                            Gson().fromJson(payloadJson, CredentialsPayload::class.java)

//                        saveTokens(credentialsPayload)

                        emitter.onSuccess(credentialsPayload)
                    },
                    onError = { throwable ->
                        emitter.onError(throwable)
                    }
                )

        }


    /**
     * In case our credentials have expired, we'll trigger this method.
     * We extract token from response in form of payload.
     *
     * @see CredentialsPayload
     * @see GetTokenExchangeDone
     */
    private fun requestCredentialsRefresh(): SingleTransformer<in TokenExchangeResponse, out TokenExchangeResponse> =
        SingleTransformer<TokenExchangeResponse, TokenExchangeResponse> {
            it.flatMap { input: TokenExchangeResponse ->

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

                        TokenExchangeResponse()
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
    private fun requestDataQuery(scope: DataRequest?): SingleTransformer<in TokenExchangeResponse, out TokenExchangeResponse> =
        SingleTransformer<TokenExchangeResponse, TokenExchangeResponse> {
            it.flatMap { input: TokenExchangeResponse ->

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

                    TokenExchangeResponse()
                        .copy(
                            consentData = GetConsentDone(session = response.session),
                            credentials = input.credentials
                        )
                }
            }
        }

    private fun getAccountsContent(
        callback: GetAccountsCallback,
        sessionKey: String,
        authHeader: String,
        credentials: CredentialsPayload
    ) {

        fun getAccounts(): SingleTransformer<in CredentialsPayload, out GetAccountsResponse> =
            SingleTransformer {
                it.flatMap { onboardPayload ->
                    Single.create { emitter ->
                        Log.d("DEBUG expire", onboardPayload.accessToken.expiresOn.toString())
                        apiClient.argonService.getFileBytes(authHeader, sessionKey, "accounts.json")
                            .map { response: Response<ResponseBody> ->

                                Log.d("DEBUG headers", response.headers().toString())
                                Log.d("DEBUG response body", response.raw().body.toString())
                                val headers = response.headers()["x-metadata"]
                                val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                                Log.d("DEBUG extracted headers", headerString)

                                val payloadHeader = Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

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

                                Log.d("DEBUG content", String(decompressedContentBytes))

                                GetAccountsResponse(
                                    Gson().fromJson(
                                        String(decompressedContentBytes),
                                        Accounts::class.java
                                    ), credentials
                                )

                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onSuccess = { result ->
                                    callback.invoke(result, null)
                                },
                                onError = { throwable ->
                                    callback.invoke(
                                        null,
                                        AuthError.ErrorWithMessage(
                                            throwable.localizedMessage ?: "Unknown error occurred"
                                        )
                                    )
                                }
                            )
                    }
                }
            }

        checkAccessToken(credentials)
            .compose(getAccounts())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result ->
                    callback.invoke(result, null)

                },
                onError = { throwable ->
                    callback.invoke(
                        null,
                        AuthError.ErrorWithMessage(
                            throwable.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    ////////////////
    /// Handlers ///
    ///////////////
    private fun handleReadAccounts(
        credentials: CredentialsPayload,
        callback: GetAccountsCallback
    ) {

        fun requestDataQuery(): SingleTransformer<in CredentialsPayload, out DataQueryResponse> =
            SingleTransformer {
                it.flatMap { credentials ->
                    Single.create { emitter ->
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

                                    response.credentials = credentials

                                    emitter.onSuccess(response)
                                }
                                error != null -> emitter.onError(error)
                                else -> emitter.onError(IllegalArgumentException())
                            }
                        }
                    }
                }
            }


        checkAccessToken(credentials)
            .compose(requestDataQuery())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        it.credentials.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    getAccountsContent(callback, it.session.key, authHeader, it.credentials)

                },
                onError = {
                    callback.invoke(
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
        callback: WriteDataCallback
    ) {
        fun writeData(): SingleTransformer<CredentialsPayload, WriteDataResponse> =
            SingleTransformer {
                it.flatMap { credentials ->
                    Single.create { emitter ->
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
                                    emitter.onSuccess(WriteDataResponse(true, credentials))
                                    DMELog.i("Successfully pushed data to postbox")
                                },
                                onError = { error ->
                                    emitter.onError(error)
                                    DMELog.i("Successfully pushed data to postbox")
                                }
                            )
                    }
                }
            }

        checkAccessToken(credentials)
            .compose(writeData())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { response ->
                    callback(
                        WriteDataResponse(response.dataWritten, response.credentials),
                        null
                    )
                },
                onError = { error ->
                    when {
                        error is APIError && error.code == "InvalidToken" -> callback(
                            WriteDataResponse(null, credentials),
                            APIError.GENERIC(message = "Failed to push file to postbox. Access token is invalid. Request new session.")
                        )
                        else -> {
                            DMELog.e("Failed to push file to postbox. Error: ${error.printStackTrace()} ${error.message}")
                            callback(
                                WriteDataResponse(null, credentials),
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
        credentials: CredentialsPayload,
        callback: FileListCallback
    ) {
        fun requestDataQuery(): SingleTransformer<in CredentialsPayload, out DataQueryResponse> =
            SingleTransformer {
                it.flatMap { credentials ->
                    Single.create { emitter ->
                        val accessToken =
                            Gson().fromJson(
                                credentials.accessToken.value,
                                UserAccessToken::class.java
                            )
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
                                    sessionKey = sessionManager.updatedSession?.key

                                    response.credentials = credentials
                                    emitter.onSuccess(response)
                                }
                                error != null -> {
                                    emitter.onError(error)
                                }
                                else -> emitter.onError(IllegalArgumentException())
                            }
                        }
                    }

                }
            }

        checkAccessToken(credentials)
            .compose(requestDataQuery())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        it.credentials.accessToken.value!!,
                        configuration.appId,
                        configuration.contractId
                    )

                    val signingKey: PrivateKey =
                        KeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                    val authHeader: String = jwt.sign(signingKey).tokenize()

                    apiClient.argonService.getFileList(authHeader, it.session.key)
                        .map {

                            it.credentials = credentials
                            it
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { callback.invoke(it, null) },
                            onError = {
                                callback.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        it.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    callback.invoke(
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
        callback: FileContentBytesCallback
    ) {

        fun requestDataQuery(): SingleTransformer<in CredentialsPayload, out DataQueryResponse> =
            SingleTransformer {
                it.flatMap { credentials ->
                    Single.create { emitter ->
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

                                    response.credentials = credentials
                                    emitter.onSuccess(response)
                                }
                                error != null -> emitter.onError(error)
                                else -> emitter.onError(IllegalArgumentException())
                            }
                        }
                    }
                }
            }

        checkAccessToken(credentials)
            .compose(requestDataQuery())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        it.credentials.accessToken.value!!,
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

                            FileItemBytes().copy(
                                fileContent = decompressedContentBytes,
                                credentials = credentials
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onSuccess = { result ->
                                callback.invoke(result, null)
                            },
                            onError = { throwable ->
                                callback.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        throwable.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    callback.invoke(
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
        callback: FileContentCallback
    ) {

        fun requestDataQuery(): SingleTransformer<in CredentialsPayload, out DataQueryResponse> =
            SingleTransformer {
                it.flatMap { credentials ->

                    Single.create { emitter ->
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

                                    response.credentials = credentials
                                    emitter.onSuccess(response)
                                }
                                error != null -> emitter.onError(error)
                                else -> emitter.onError(IllegalArgumentException())
                            }
                        }
                    }
                }
            }

        checkAccessToken(credentials)
            .compose(requestDataQuery())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    DMELog.i(context.getString(R.string.labelReadingFiles))
                    sessionManager.updatedSession = it.session
                    sessionKey = sessionManager.updatedSession?.key

                    val jwt = PermissionAccessRequestJWT(
                        it.credentials.accessToken.value!!,
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
                                callback.invoke(result, null)
                            },
                            onError = { throwable ->
                                callback.invoke(
                                    null,
                                    AuthError.ErrorWithMessage(
                                        throwable.localizedMessage ?: "Unknown error occurred"
                                    )
                                )
                            }
                        )
                },
                onError = {
                    callback.invoke(
                        null,
                        APIError.ErrorWithMessage(
                            it.localizedMessage ?: context.getString(R.string.labelUnknownError)
                        )
                    )
                }
            )
    }

    private fun handleCyclicDataDownload(
        scope: DataRequest?,
        credentials: CredentialsPayload,
        downloadHandler: FileContentCallback,
        callback: FileListCallback
    ) {

        fun requestDataQuery(): SingleTransformer<in CredentialsPayload, out DataQueryResponse> =
            SingleTransformer {
                it.flatMap { credentials ->
                    Single.create { emitter ->

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

                                    response.credentials = credentials

                                    emitter.onSuccess(response)
                                }
                                error != null -> emitter.onError(error)
                                else -> emitter.onError(IllegalArgumentException())
                            }
                        }
                    }
                }
            }

        checkAccessToken(credentials)
            .compose(requestDataQuery())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    sessionManager.updatedSession = it.session
                    sessionKey = sessionManager.updatedSession?.key

                    handleContinuousDataDownload(it.credentials, downloadHandler, callback)
                },
                onError = {
                    callback.invoke(
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
        fileContentCallback: FileContentCallback,
        fileListCallback: FileListCallback
    ) {
        DMELog.i(context.getString(R.string.labelReadingFiles))

        activeFileDownloadHandler = fileContentCallback
        activeSessionDataFetchCallbackHandler = fileListCallback

        getSessionFileList(credentials, { _, updatedFileIds ->

            updatedFileIds.forEach {

                activeDownloadCount++
                DMELog.i("Downloading file with ID: $it.")

                getSessionData(it, credentials) { file, error ->

                    when {
                        file != null -> DMELog.i("Successfully downloaded updates for file with ID: $it.")
                        else -> DMELog.e("Failed to download updates for file with ID: $it.")
                    }

                    fileContentCallback.invoke(file, error)
                    activeDownloadCount--
                }
            }

        }) { fileList, error ->
            fileList?.credentials = credentials
            if (fileList?.syncStatus == FileList.SyncStatus.COMPLETED() && error == null && fileList.accounts?.hasError() == false && activeDownloadCount == 0) {
                syncRunning = false
                fileListCallback(
                    fileList,
                    null
                ) // We only want to push this if the error exists, else
                // it'll cause a premature loop exit.
            } else if (fileList?.accounts?.hasError() == true) {
                val reAuthError = fileList.accounts.parseError()
                reAuthError.accounts = fileList.accounts.errorAccounts()
                syncRunning = false
                fileListCallback(
                    fileList,
                    reAuthError
                )
            } else if (fileList?.syncStatus == FileList.SyncStatus.PARTIAL()) {
                syncRunning = false
                fileListCallback(
                    fileList,
                    error
                )
            }
        }
    }
}