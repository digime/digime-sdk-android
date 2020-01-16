package me.digi.sdk

abstract class DMEError(override val message: String): Throwable(message)

sealed class DMESDKError(override val message: String): DMEError(message) {

    class NoContract(): DMESDKError("Contract ID not set.")
    class InvalidContract(): DMESDKError("Contract ID is not in a valid format.")
    class DecryptionFailed(): DMESDKError("Could not decrypt the file content.")
    class EncryptionFailed(): DMESDKError("Could not encrypt the file content.")
    class InvalidData(): DMESDKError("Could not deserialise the file content.")
    class InvalidVersion(): DMESDKError("Current version of SDK no longer supported.")
    class NoAppID(): DMESDKError("App ID not set.")
    class P12ParsingError(): DMESDKError("Could not parse the P12 file with supplied password, or no P12/password given.")
    class NoURLScheme(): DMESDKError("Intent filter for URL scheme (for callbacks) not found.")
    class DigiMeAppNotFound(): DMESDKError("Querying digime schema failed. (digi.me app not installed.)")
    class CommunicatorNotInitialized(): DMESDKError("DMEAppCommunicator shared instance accessed before initialization.")
    class InvalidContext(): DMESDKError("Given context is not the application context; ONLY the application context may be used.")
    class FileListPollingTimeout(): DMESDKError("File List time out has been reached as there have been no changes during the number of retries specified in `DMEPullConfiguration`.")

}

sealed class DMEAuthError(override val message: String): DMEError(message) {

    class General(): DMEAuthError("An unknown authorisation error has occurred.")
    class Cancelled(): DMEAuthError("The user cancelled the authorisation action.")
    class InvalidSession(): DMEAuthError("The session key is invalid or has expired.")
    class InvalidSessionKey(): DMEAuthError("The session key provided to the digi.me app is not valid.")

}

sealed class DMEAPIError(override val message: String,
                         internal val reference: String? = null,
                         internal val code: String? = null): DMEError(message) {

    class Generic(): DMEAPIError("There was a problem with your request.")
    class Server(message: String, reference: String?, code: String?): DMEAPIError(message, reference, code)
    class Unreachable(): DMEAPIError("Couldn't reach the digi.me API - please check your network connection.")
    class InvalidSyncState(): DMEAPIError("An invalid sync syncStatus was returned - please try again.")
    class PartialSync(): DMEAPIError("One or more accounts failed to sync. Some data has been delivered.")
    class TokenInvalid(): DMEAPIError("The provided refresh token is invalid.")
    class TokenNotYetValid(): DMEAPIError("The provided token is not yet valid.")
    class TooManyRequest(): DMEAPIError("Rate limiting is enforced and this request exceeds the configured limit.")
    class SessionInvalid(): DMEAPIError("Session state does not exist.")
    class SessionUsed(): DMEAPIError("Session has already been consumed.")
    class SessionUpdateFailed(): DMEAPIError("Unable to update the PUA object.")

}