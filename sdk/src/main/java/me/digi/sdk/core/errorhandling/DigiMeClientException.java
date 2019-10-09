/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.errorhandling;

@SuppressWarnings("SameParameterValue")
public class DigiMeClientException extends DigiMeException {
    public DigiMeClientException() {
        super();
    }

    public DigiMeClientException(String message) {
        super(message);
    }

    public DigiMeClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DigiMeClientException(Throwable throwable) {
        super(throwable);
    }
}
