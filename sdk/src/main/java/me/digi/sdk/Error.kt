package me.digi.sdk

import me.digi.sdk.entities.FileListAccount

abstract class Error(override val message: String) : Throwable(message)

sealed class SDKError(override val message: String) : Error(message) {

    class NoContract :
        SDKError("No contracts registered! You must have forgotten to set contractId property on the configuration object.")

    class InvalidContract : SDKError("Provided contractId has invalid format.")
    class DecryptionFailed : SDKError("Could not decrypt file content.")
    class EncryptionFailed : SDKError("Could not encrypt the file content.")
    class InvalidData : SDKError("Could not serialize data.")
    class InvalidVersion :
        SDKError("This SDK version is no longer supported.  Please update to a newer version.")

    class NoAppID :
        SDKError("No application registered! Please set appId property on the configuration object.")

    class P12ParsingError :
        SDKError("Could not parse the P12 file with supplied password, or no P12/password given.")

    class NoURLScheme : SDKError("Intent filter for URL scheme (for callbacks) not found.")
    class DigiMeAppNotFound : SDKError("DigiMe app is not installed")
    class CommunicatorNotInitialized :
        SDKError("AppCommunicator shared instance accessed before initialization.")

    class InvalidContext :
        SDKError("Given context is not the application context; ONLY the application context may be used.")

    class FileListPollingTimeout :
        SDKError("File List time out reached as there have been no changes during the number of retries specified in `PullConfiguration`.")
}

sealed class AuthError(override val message: String) : Error(message) {

    class General : AuthError("Unknown authorization error has occurred.")
    object InitCheck : AuthError("Parameters passed in didn't pass initial checks.")
    object InvalidCode : AuthError("Code passed in was not valid.")
    object Onboard : AuthError("There was an error when trying to onboard the given service.")
    object Server : AuthError("An error is received from a server call.")
    object Cancelled : AuthError("User cancelled authorization.")
    class InvalidSession : AuthError("The session key is invalid or has expired.")
    class InvalidSessionKey : AuthError("digi.me app returned an invalid session key.")
    class TokenExpired :
        AuthError("The refresh token supplied has expired. As `autoRecoverExpiredCredentials` is turned off, you will need to repeat authorization without credentials to recover.")

    class ErrorWithMessage(message: String) : AuthError(message)
}

sealed class PushDataError(override val message: String) : Error(message) {
    object General : PushDataError("Unable to push data to postbox")
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArgonCode(val value: String)
sealed class APIError(
    internal var code: String? = null,
    override var message: String,
    internal var reference: String? = null,
    var accounts: List<FileListAccount>? = null

) : Error(message) {

    constructor() : this(null, "", null)
    constructor(argonCode: String) : this(argonCode, "", null)

    init {
        if (code == null) {
            // No override provided, try to parse annotation.
            code = (this::class.annotations.firstOrNull { it is ArgonCode } as? ArgonCode)?.value
        }
    }

    @ArgonCode("InvalidPCloud")
    class INVALID_PCLOUD : APIError()

    @ArgonCode("InvalidRefreshToken")
    class INVALID_REFRESH_TOKEN : APIError()

    @ArgonCode("InvalidSyncState")
    class INVALID_SYNC_STATE : APIError()

    @ArgonCode("PartialSync")
    class PARTIAL_SYNC : APIError()

    @ArgonCode("TokenInvalid")
    class INVALID_TOKEN : APIError()

    @ArgonCode("TokenNotYetValid")
    class TOKEN_NOT_YET_VALID : APIError()

    @ArgonCode("TooManyRequests")
    class TOO_MANY_REQUESTS : APIError()

    @ArgonCode("SessionInvalid")
    class SESSION_INVALID : APIError()

    @ArgonCode("SessionUsed")
    class SESSION_USED : APIError()

    @ArgonCode("SessionUpdateFailed")
    class SESSION_UPDATE_FAILED : APIError()

    class UNMAPPED(
        argonCode: String? = "",
        argonMessage: String = "",
        argonReference: String? = ""
    ) : APIError(argonCode, argonMessage, argonReference)

    class GENERIC(httpStatusCode: Int? = 0, message: String? = "") :
        APIError(message = "Http Status: $httpStatusCode\nError: $message")

    class ErrorWithMessage(message: String) : AuthError(message)
    class UNREACHABLE :
        APIError(message = "Couldn't reach the digi.me API - please check your network connection.")

    class REAUTHREQUIRED :
        APIError(message = "Reauthorization required.")
}