package me.digi.sdk.unify

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
import me.digi.sdk.api.helpers.DMEMultipartBody
import me.digi.sdk.callbacks.*
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.entities.payload.PreAuthorizationCodePayload
import me.digi.sdk.entities.payload.TokenReferencePayload
import me.digi.sdk.entities.request.*
import me.digi.sdk.entities.response.*
import me.digi.sdk.interapp.managers.SaasConsentManager
import me.digi.sdk.utilities.DMECompressor
import me.digi.sdk.utilities.DMEFileListItemCache
import me.digi.sdk.utilities.DMELog
import me.digi.sdk.utilities.crypto.*
import me.digi.sdk.utilities.jwt.*
import java.security.PrivateKey
import kotlin.math.max

class DigiMeClient(
    val context: Context,
    val configuration: DigiMeConfiguration
) : DMEClient(
    context = context,
    config = configuration
) {

    private val authorizeConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(configuration.baseUrl, "authorize")
    }

    private val onboardConsentManager: SaasConsentManager by lazy {
        SaasConsentManager(configuration.baseUrl, type = "onboard")
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

    /**
     *
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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { result: GetTokenExchangeDone ->
                    sessionManager.updatedSession = result.consentData.session

                    val response = AuthorizationResponse(
                        sessionKey = result.consentData.session.key,
                        postboxData = OngoingPostboxData(
                            postboxId = result.consentData.consentResponse.postboxId,
                            publicKey = result.consentData.consentResponse.publicKey
                        ),
                        credentials = EssentialCredentials(
                            result.credentials.accessToken.value,
                            result.credentials.refreshToken.value
                        )
                    )

                    completion.invoke(response, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError }
                            ?: DMEAPIError.ErrorWithMessage(
                                error.localizedMessage ?: "Unknown error occurred"
                            ))
                }
            )
    }

    /**
     *
     */
    fun authorizeOngoingWriteAccess(
        fromActivity: Activity,
        postbox: OngoingPostboxData? = null,
        credentials: CredentialsPayload? = null, // Change to only have access/refresh token values
        completion: GetAuthorizationDoneCompletion
    ) {

        var activeCredentials: CredentialsPayload? = credentials
        var activePostbox: OngoingPostboxData? = postbox

        // First, we request pre-auth code needed for authorization consent manager.
        // In this instance, we don't need scope, hence it's defaulted to null.
        requestPreAuthorizationCode(credentials, scope = null)
            // Next, we check if any credentials were supplied (for access restoration).
            // If not, we kick the user out of the flow and authorize normally.
            .let { preAuthorizationCodeResponse: Single<out GetPreAuthCodeDone> ->
                if (activeCredentials != null && activePostbox != null)
                    preAuthorizationCodeResponse.map {
                        GetTokenExchangeDone()
                            .copy(
                                consentData = GetConsentDone(
                                    session = it.session,
                                    consentResponse = ConsentAuthResponse(
                                        postboxId = activePostbox?.postboxId,
                                        publicKey = activePostbox?.publicKey
                                    )
                                ),
                                credentials = activeCredentials!!
                            )
                    } else preAuthorizationCodeResponse
                    .compose(requestConsentAccess(fromActivity, serviceId = null))
                    .compose(requestTokenExchange())
                    .doOnSuccess { tokenExchangeResponse ->
                        activeCredentials = tokenExchangeResponse.credentials
                        activePostbox = OngoingPostboxData(
                            postboxId = tokenExchangeResponse.consentData.consentResponse.postboxId,
                            publicKey = tokenExchangeResponse.consentData.consentResponse.publicKey,
                        )
                    }
            }
            .onErrorResumeNext { error ->
                errorHandler(
                    error,
                    credentials,
                    scope = null,
                    fromActivity,
                    serviceId = null,
                    activeCredentials
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {

                    sessionManager.updatedSession = it.consentData.session

                    val response = AuthorizationResponse(
                        sessionKey = it.consentData.session.key,
                        postboxData = OngoingPostboxData(
                            postboxId = it.consentData.consentResponse.postboxId,
                            publicKey = it.consentData.consentResponse.publicKey
                        ),
                        credentials = EssentialCredentials(
                            accessToken = it.credentials.accessToken.value,
                            refreshToken = it.credentials.refreshToken.value
                        )
                    )

                    completion.invoke(response, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError }
                            ?: DMEAPIError.ErrorWithMessage(error.localizedMessage)
                    )
                }
            )
    }

    fun authorizeOngoingReadAccess(
        fromActivity: Activity,
        scope: DataRequest? = null,
        credentials: CredentialsPayload? = null, // Change to only have access/refresh token values
        serviceId: String? = null,
        completion: GetAuthorizationDoneCompletion
    ) {

        var activeCredentials: CredentialsPayload? = credentials

        // First, we request pre-auth code needed for authorization consent manager.
        requestPreAuthorizationCode(credentials, scope)
            // Next, we check if any credentials were supplied (for access restoration).
            // If not, we kick the user out of the flow to authorise normally.
            .let { preAuthorizationCodeResponse: Single<out GetPreAuthCodeDone> ->
                if (activeCredentials != null)
                    preAuthorizationCodeResponse.map {
                        GetTokenExchangeDone()
                            .copy(
                                consentData = GetConsentDone(session = it.session),
                                credentials = activeCredentials!!
                            )
                    }
                else preAuthorizationCodeResponse
                    .compose(requestConsentAccess(fromActivity, serviceId))
                    .compose(requestTokenExchange())
                    .doOnSuccess { tokenExchangeResponse ->
                        activeCredentials = tokenExchangeResponse.credentials
                    }
            }
            // At this point, we have a session and a set of credentials, so we can trigger
            // the data query to 'prepare' data to be synced'.
            .compose(requestDataQuery(scope))
            .onErrorResumeNext { error ->
                errorHandler(
                    error,
                    credentials,
                    scope,
                    fromActivity,
                    serviceId,
                    activeCredentials
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {

                    sessionManager.updatedSession = it.consentData.session

                    val response = AuthorizationResponse(
                        sessionKey = it.consentData.session.key,
                        credentials = EssentialCredentials(
                            it.credentials.accessToken.value,
                            it.credentials.refreshToken.value
                        )
                    )

                    completion.invoke(response, null)
                },
                onError = { error ->
                    completion.invoke(
                        null,
                        error.let { it as? DMEError }
                            ?: DMEAPIError.ErrorWithMessage(error.localizedMessage)
                    )
                }
            )
    }

    private fun errorHandler(
        error: Throwable?,
        credentials: CredentialsPayload?,
        scope: DataRequest?,
        fromActivity: Activity,
        serviceId: String?,
        activeCredentials: CredentialsPayload?
    ): SingleSource<out GetTokenExchangeDone>? {
        var activeCredentials1 = activeCredentials
        return if (error is DMEAPIError && error.code == "InternalServerError") {

            requestPreAuthorizationCode(credentials, scope)
                .compose(requestConsentAccess(fromActivity, serviceId))
                .compose(requestTokenExchange())
                .doOnSuccess { activeCredentials1 = it.credentials }

            // If an error we encountered is a "InvalidToken" error, which means that the ACCESS token
            // has expired.
        } else if (error is DMEAPIError && error.code == "InvalidToken") {
            // If so, we take the active session and expired credentials and try to refresh them.

            requestPreAuthorizationCode(credentials, scope)
                .map { response: GetPreAuthCodeDone ->
                    GetTokenExchangeDone()
                        .copy(
                            consentData = GetConsentDone(session = response.session),
                            credentials = activeCredentials1!!
                        )
                }
                .compose(requestCredentialsRefresh())
                .doOnSuccess { activeCredentials1 = it.credentials }
                .onErrorResumeNext { error ->

                    // If an error is encountered from this call, we inspect it to see if it's an
                    // 'InvalidToken' error, meaning that the REFRESH token has expired.
                    if (error is DMEAPIError && error.code == "InvalidToken") {
                        // If so, we need to obtain a new set of credentials from the digi.me
                        // application. Process the flow as before, for ongoing access, provided
                        // that auto-recover is enabled. If not, we throw a specific error and
                        // exit the flow.
                        if (configuration.autoRecoverExpiredCredentials) {
                            requestPreAuthorizationCode(credentials, scope)
                                .compose(requestConsentAccess(fromActivity, serviceId))
                                .compose(requestTokenExchange())
                                .doOnSuccess { activeCredentials1 = it.credentials }

                                // Once new credentials are obtained, re-trigger the data query.
                                // If it fails here, credentials are not the issue. The error
                                // will be propagated down to the callback as normal.
                                .compose(requestDataQuery(scope))
                        } else Single.error(DMEAuthError.TokenExpired())
                    } else Single.error(error)
                }
        } else Single.error(error)
    }

    /**
     *
     */
    fun updateSession(
        sessionRequest: DMESessionRequest,
        completion: GetSessionCompletion
    ) {

        fun requestSession(sessionRequest: DMESessionRequest): Single<SessionResponse> =
            Single.create { emitter ->
                apiClient.makeCall(apiClient.argonService.getSession(sessionRequest)) { sessionResponse, error ->
                    when {
                        sessionResponse != null -> emitter.onSuccess(sessionResponse)
                        error != null -> emitter.onError(error)
                        else -> emitter.onError(IllegalArgumentException())
                    }
                }
            }

        requestSession(sessionRequest)
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
                        DMEAuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    /**
     *
     */
    fun deleteUser(
        accessToken: String?,
        completion: DMEUserLibraryDeletion
    ) {
        DMELog.i(context.getString(R.string.labelDeleteLibrary))

        fun deleteLibrary() = Single.create<Boolean> { emitter ->
            accessToken?.let {

                val jwt = DMEUserDeletionRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    accessToken
                )

                val signingKey: PrivateKey =
                    DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
                val authHeader: String = jwt.sign(signingKey).tokenize()

                apiClient.makeCall(apiClient.argonService.deleteUser(authHeader)) { _, error ->
                    when {
                        error != null -> emitter.onError(error)
                        else -> emitter.onSuccess(true)
                    }
                }
            }
                ?: emitter.onError(DMEAPIError.ErrorWithMessage(context.getString(R.string.labelAccessTokenInvalidOrMissing)))
        }

        deleteLibrary()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    completion.invoke(it, null)
                },
                onError = {
                    it.localizedMessage?.let { message ->
                        if (message.contains("204"))
                            completion.invoke(true, null)
                        else completion.invoke(
                            null,
                            DMEAPIError.ErrorWithMessage(message)
                        )
                    }
                }
            )
    }

    /**
     *
     */
    fun writeData(
        postboxFile: DMEPushPayload?,
        accessToken: String,
        completion: DMEOngoingPostboxPushCompletion
    ) {
        DMELog.i("Initializing push data to postbox.")

        val postbox = postboxFile as DMEPushPayload

        if (sessionManager.isSessionValid()) {
            val encryptedData = DMEDataEncryptor.encryptedDataFromBytes(
                postbox.postbox.publicKey!!,
                postbox.content,
                postbox.metadata
            )

            val multipartBody: DMEMultipartBody = DMEMultipartBody.Builder()
                .postboxPushPayload(postbox)
                .dataContent(encryptedData.fileContent, postbox.mimeType)
                .build()

            val jwt = DMEAuthTokenRequestJWT(
                accessToken,
                encryptedData.iv,
                encryptedData.metadata,
                encryptedData.symmetricalKey,
                configuration.appId,
                configuration.contractId
            )

            val signingKey: PrivateKey =
                DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
            val authHeader: String = jwt.sign(signingKey).tokenize()

            apiClient.argonService.pushOngoingData(
                authHeader,
                postbox.postbox.key!!,
                encryptedData.symmetricalKey,
                encryptedData.iv,
                encryptedData.metadata,
                postbox.postbox.postboxId!!,
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
                            error is DMEAPIError && error.code == "InvalidToken" -> completion(
                                null,
                                DMEAPIError.GENERIC(message = "Failed to push file to postbox. Access token is invalid. Request new session.")
                            )
                            else -> {
                                DMELog.e("Failed to push file to postbox. Error: ${error.printStackTrace()} ${error.message}")
                                completion(
                                    null,
                                    DMEAuthError.ErrorWithMessage(error.localizedMessage)
                                )
                            }
                        }
                    }
                )
        } else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    /**
     *
     */
    fun readData(
        downloadHandler: DMEFileContentCompletion,
        completion: DMEFileListCompletion
    ) {

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

    /**
     *
     */
    fun onboardService(
        fromActivity: Activity,
        serviceId: String,
        accessToken: String,
        completion: OnboardingCompletion
    ) {

        fun requestCodeReference(): Single<TokenReferencePayload> = Single.create { emitter ->
            DMELog.i(context.getString(R.string.labelReferenceOnboardingCode))

            val jwt = DMEReferenceCodeRequestJWT(
                configuration.appId,
                configuration.contractId,
                accessToken
            )

            val signingKey: PrivateKey =
                DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
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

                    val jwt = DMETriggerDataQueryRequestJWT(
                        configuration.appId,
                        configuration.contractId,
                        accessToken
                    )

                    val signingKey: PrivateKey =
                        DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
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
                        DMEAuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    /**
     *
     */
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
                    completion.invoke(
                        null,
                        DMEAuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Could not fetch services. Something went wrong"
                        )
                    )
                }
            )
    }

    /**
     *
     */
    private fun getSessionData(fileId: String, completion: DMEFileContentCompletion) {

        val currentSession = sessionManager.updatedSession

        if (currentSession != null && sessionManager.isSessionValid()) {

            apiClient.argonService.getFileBytes(currentSession.key, fileId)
                .map { response ->
                    val headers = response.headers()["X-Metadata"]
                    val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                    val payloadHeader =
                        Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                    val result: ByteArray = response.body()?.byteStream()?.readBytes() as ByteArray

                    val contentBytes: ByteArray =
                        DMEDataDecryptor.dataFromEncryptedBytes(result, configuration.privateKeyHex)

                    val compression: String = try {
                        payloadHeader.compression
                    } catch (e: Throwable) {
                        DMECompressor.COMPRESSION_NONE
                    }
                    val decompressedContentBytes: ByteArray =
                        DMECompressor.decompressData(contentBytes, compression)

                    DMEFile().copy(fileContent = String(decompressedContentBytes))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { completion.invoke(it, null) },
                    onError = {
                        completion.invoke(
                            null,
                            DMEAuthError.ErrorWithMessage(
                                it.localizedMessage ?: "Unknown error occurred"
                            )
                        )
                    }
                )
        } else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    /**
     *
     */
    fun getFileByName(fileId: String, sessionKey: String, completion: DMEFileContentCompletion) {

        apiClient.argonService.getFileBytes(sessionKey, fileId)
            .map { response ->
                val headers = response.headers()["X-Metadata"]
                val headerString = String(Base64.decode(headers, Base64.DEFAULT))
                val payloadHeader =
                    Gson().fromJson(headerString, HeaderMetadataPayload::class.java)

                val result: ByteArray = response.body()?.byteStream()?.readBytes() as ByteArray

                val contentBytes: ByteArray =
                    DMEDataDecryptor.dataFromEncryptedBytes(result, configuration.privateKeyHex)

                val compression: String = try {
                    payloadHeader.compression
                } catch (e: Throwable) {
                    DMECompressor.COMPRESSION_NONE
                }
                val decompressedContentBytes: ByteArray =
                    DMECompressor.decompressData(contentBytes, compression)

                DMEFile().copy(fileContent = String(decompressedContentBytes))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { completion.invoke(it, null) },
                onError = {
                    completion.invoke(
                        null,
                        DMEAuthError.ErrorWithMessage(
                            it.localizedMessage ?: "Unknown error occurred"
                        )
                    )
                }
            )
    }

    /**
     *
     */
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

    /**
     *
     */
    fun getFileList(completion: DMEFileListCompletion) {

        val currentSession = sessionManager.updatedSession

        if (currentSession != null && sessionManager.isSessionValid()) {
            apiClient.makeCall(apiClient.argonService.getFileList(currentSession.key), completion)
        } else {
            DMELog.e("Your session is invalid; please request a new one.")
            completion(null, DMEAuthError.InvalidSession())
        }
    }

    /**
     *
     */
    private fun scheduleNextPoll(immediately: Boolean = false) {

        DMELog.d("Session data poll scheduled.")

        val delay = (if (immediately) 0 else (max(configuration.pollInterval, 3) * 1000).toLong())
        Handler(Looper.getMainLooper()).postDelayed({

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

    /**
     *
     */
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

    /**
     * Defined bellow are a number of 'modules' that are used within the Cyclic flow.
     * These can be combined in various ways as the auth state demands.
     * See the flow below for details.
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
                DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))

            val jwt = if (credentials != null)
                DMEPreauthorizationRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    codeVerifier,
                    credentials.accessToken.value
                )
            else DMEPreauthorizationRequestJWT(
                configuration.appId,
                configuration.contractId,
                codeVerifier
            )

            val signingKey = DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
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
            ) { response: PreAuthorizationResponse?, error: DMEError? ->
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
                        ) { consentResponse: ConsentAuthResponse?, error: DMEError? ->
                            when {
                                consentResponse != null -> {
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
     * Once consent is completed, we triger this method to get fresh set of credentials.
     * @see CredentialsPayload
     */
    private fun requestTokenExchange(): SingleTransformer<in GetConsentDone, out GetTokenExchangeDone> =
        SingleTransformer<GetConsentDone, GetTokenExchangeDone> {
            it.flatMap { input: GetConsentDone ->
                val codeVerifier =
                    input.session.metadata[context.getString(R.string.key_code_verifier)].toString()

                val jwt = DMEAuthCodeExchangeRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    input.consentResponse.code!!,
                    codeVerifier
                )

                val signingKey = DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
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
                    DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
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

                val jwt = DMETriggerDataQueryRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    input.credentials.accessToken.value!!
                )

                val signingKey: PrivateKey =
                    DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex)
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
}