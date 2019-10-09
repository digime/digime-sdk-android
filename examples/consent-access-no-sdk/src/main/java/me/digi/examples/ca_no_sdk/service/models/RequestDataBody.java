/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service.models;

import com.google.gson.annotations.SerializedName;

public class RequestDataBody {
    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("fileName")
    public String fileName;

    public RequestDataBody(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public RequestDataBody(String sessionKey, String fileName) {
        this.sessionKey = sessionKey;
        this.fileName = fileName;
    }
}
