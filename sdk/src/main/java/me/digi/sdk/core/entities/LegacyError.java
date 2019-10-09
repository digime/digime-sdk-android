/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;

public class LegacyError extends ServerError {
    @SerializedName("error")
    public String error;

    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    @Override
    public String errorCode() {
        return error;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String reference() {
        return String.valueOf(code);
    }
}
