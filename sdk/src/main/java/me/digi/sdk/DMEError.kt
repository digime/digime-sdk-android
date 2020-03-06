package me.digi.sdk

abstract class DMEError(override val message: String) : Throwable(message)

sealed class DMESDKError(override val message: String) : DMEError(message) {

    class NoContract : DMESDKError("No contracts registered! You must have forgotten to set contractId property on the configuration object.")
    class InvalidContract : DMESDKError("Provided contractId has invalid format.")
    class DecryptionFailed : DMESDKError("Could not decrypt file content.")
    class EncryptionFailed : DMESDKError("Could not encrypt the file content.")
    class InvalidData : DMESDKError("Could not serialize data.")
    class InvalidVersion : DMESDKError("This SDK version is no longer supported.  Please update to a newer version.")
    class NoAppID : DMESDKError("No application registered! Please set appId property on the configuration object.")
    class P12ParsingError : DMESDKError("Could not parse the P12 file with supplied password, or no P12/password given.")
    class NoURLScheme : DMESDKError("Intent filter for URL scheme (for callbacks) not found.")
    class DigiMeAppNotFound : DMESDKError("DigiMe app is not installed")
    class CommunicatorNotInitialized : DMESDKError("DMEAppCommunicator shared instance accessed before initialization.")
    class InvalidContext : DMESDKError("Given context is not the application context; ONLY the application context may be used.")
    class FileListPollingTimeout : DMESDKError("File List time out reached as there have been no changes during the number of retries specified in `DMEPullConfiguration`.")
}

sealed class DMEAuthError(override val message: String) : DMEError(message) {

    class General : DMEAuthError("Unknown authorization error has occurred.")
    class Cancelled : DMEAuthError("User cancelled authorization.")
    class InvalidSession : DMEAuthError("The session key is invalid or has expired.")
    class InvalidSessionKey : DMEAuthError("digi.me app returned an invalid session key.")
    class TokenExpired : DMEAuthError("The refresh token supplied has expired. As `autoRecoverExpiredCredentials` is turned off, you will need to repeat authorization without credentials to recover.")

}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArgonCode(val value: String)
sealed class DMEAPIError(
    internal var code: String? = null,
    override var message: String,
    internal var reference: String? = null

) : DMEError(message) {

    constructor(): this(null, "", null)
    constructor(argonCode: String): this(argonCode, "", null)

    init {
        if (code == null) {
            // No override provided, try to parse annotation.
            code = (this::class.annotations.firstOrNull { it is ArgonCode } as? ArgonCode)?.value
        }
    }

    @ArgonCode("InvalidPCloud") class INVALID_PCLOUD: DMEAPIError()
    @ArgonCode("InvalidRefreshToken") class INVALID_REFRESH_TOKEN: DMEAPIError()
    @ArgonCode("InvalidSyncState") class INVALID_SYNC_STATE : DMEAPIError()
    @ArgonCode("PartialSync") class PARTIAL_SYNC : DMEAPIError()
    @ArgonCode("TokenInvalid") class INVALID_TOKEN: DMEAPIError()
    @ArgonCode("TokenNotYetValid") class TOKEN_NOT_YET_VALID: DMEAPIError()
    @ArgonCode("TooManyRequests") class TOO_MANY_REQUESTS : DMEAPIError()
    @ArgonCode("SessionInvalid") class SESSION_INVALID: DMEAPIError()
    @ArgonCode("SessionUsed") class SESSION_USED: DMEAPIError()
    @ArgonCode("SessionUpdateFailed") class SESSION_UPDATE_FAILED: DMEAPIError()

    class UNMAPPED(argonCode: String, argonMessage: String?, argonReference: String?): DMEAPIError(argonCode, argonMessage!!, argonReference)
    class GENERIC(httpStatusCode: Int, message: String): DMEAPIError(message = "Http Status: $httpStatusCode\nError: $message")
    class UNREACHABLE : DMEAPIError(message = "Couldn't reach the digi.me API - please check your network connection. Alternatively, the request may have timed out.")
}