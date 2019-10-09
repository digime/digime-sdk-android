/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.entities;

public abstract class ServerError {
    private int responseCode;

    public abstract String errorCode();

    public abstract String message();

    public abstract String reference();

    public int code() {
        return responseCode;
    }

    public void setCode(int code) {
        responseCode = code;
    }
}
