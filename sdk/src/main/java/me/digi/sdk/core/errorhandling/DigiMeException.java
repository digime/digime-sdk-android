/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.errorhandling;

@SuppressWarnings("SameParameterValue")
public class DigiMeException extends RuntimeException {

    public DigiMeException() {
        super();
    }

    public DigiMeException(String msg) {
        super(msg);
    }

    public DigiMeException(String format, Object... args) {
        this(String.format(format, args));
    }

    public DigiMeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DigiMeException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
