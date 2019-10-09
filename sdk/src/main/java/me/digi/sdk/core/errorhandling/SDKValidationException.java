/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.errorhandling;

public class SDKValidationException extends SDKException {
    public static final int GENERAL_VALIDATION_ERROR = 101;
    public static final int SESSION_VALIDATION_ERROR = 1001;
    public static final int CONTRACT_VALIDATION_ERROR = 2001;

    public SDKValidationException(String detail) {
        super(detail, GENERAL_VALIDATION_ERROR);
    }

    public SDKValidationException(String detail, int code) {
        super(detail, code);
    }

    public SDKValidationException(String detail, Throwable throwable) {
        super(detail, throwable, GENERAL_VALIDATION_ERROR);
    }

    public SDKValidationException(String detail, Throwable throwable, int code) {
        super(detail, throwable, code);
    }

}
