package me.digi.sdk.core.errorhandling;

public enum APIError {

    INVALID_CA_APPLICATION("InvalidConsentAccessApplication", "This app is no longer valid for Consent Access"),
    INVALID_SDK_VERSION("SDKVersionInvalid", "This version of the SDK is no longer supported, please update"),
    UNKNOWN("", "");

    public final String errorCode;
    public final String userMessage;

    APIError(String errorCode, String userMessage) {
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public static APIError errorFromCode(String errorCode) {
        for (APIError error : values()) {
            if (error.errorCode.equalsIgnoreCase(errorCode)) {
                return error;
            }
        }
        return UNKNOWN;
    }
}
