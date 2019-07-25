package me.digi.sdk

abstract class DMEError(override val message: String): Throwable(message)

sealed class DMESDKError(override val message: String): DMEError(message) {

    data class NoContract(override val message: String = "Contract ID not set."): DMESDKError(message)
    data class InvalidContract(override val message: String = "Contract ID is not in a valid format."): DMESDKError(message)
    data class DecryptionFailed(override val message: String = "Could not decrypt the file content."): DMESDKError(message)
    data class InvalidData(override val message: String = "Could not deserialise the file content."): DMESDKError(message)
    data class InvalidVersion(override val message: String = "Current version of SDK no longer supported."): DMESDKError(message)
    data class NoAppID(override val message: String = "App ID not set."): DMESDKError(message)
    data class P12ParsingError(override val message: String = "Could not parse the P12 file with supplied password, or no P12/password given."): DMESDKError(message)
    data class NoURLScheme(override val message: String = "Intent filter for URL scheme (for callbacks) not found."): DMESDKError(message)
    data class DigiMeAppNotFound(override val message: String = "Querying digime schema failed. (digi.me app not installed.)"): DMESDKError(message)

}

sealed class DMEAuthError(override val message: String): DMEError(message) {

    data class General(override val message: String = "An unknown authorisation error has occurred."): DMEAuthError(message)
    data class Cancelled(override val message: String = "The user cancelled the authorisation action."): DMEAuthError(message)
    data class InvalidSession(override val message: String = "The session key is invalid or has expired."): DMEAuthError(message)
    data class InvalidSessionKey(override val message: String = "The session key provided to the digi.me app is not valid."): DMEAuthError(message)

}

sealed class DMEAPIError(override val message: String): DMEError(message) {

    data class Generic(override val message: String): DMEAPIError(message)

}