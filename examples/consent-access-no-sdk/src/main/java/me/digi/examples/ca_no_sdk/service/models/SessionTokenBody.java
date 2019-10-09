/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service.models;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("SameParameterValue")
public class SessionTokenBody {
    @SerializedName("appId")
    public String appId;

    @SerializedName("contractId")
    public String contractId;

    public SessionTokenBody(String appId, String contractId) {
        this.appId = appId;
        this.contractId = contractId;
    }
}
