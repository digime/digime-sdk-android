/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.legacy.crypto;

/**
 * List of failures which can occur when vault is being accessed
 */
public enum FailureCause {
    INVALID_KEY_FAILURE("Bad key provided!", 500),
    AES_DECRYPTION_FAILURE("Internal error, please contact support. (%d)", 501),
    FILE_READING_FAILURE("Internal error, please contact support. (%d)", 502),
    DATA_CORRUPTED_FAILURE("Data is corrupted - file has been tampered with, please start again. (%s)", 503),
    CHECKSUM_CORRUPTED_FAILURE("Data is corrupted - file has been tampered with, please start again. (%s)", 506),
    RSA_DECRYPTION_FAILURE("Failed to decrypt with asymmetric key", 504),
    RSA_BAD_PROVIDER_FAILURE("Failed to decrypt with specified provider", 505),
    KEY_LOAD_FAILURE("Could not load private key", 506);

    private final String message;
    private final int errorCode;

    FailureCause(String message, int errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public String message() {
        return String.format(message, errorCode);
    }
}
