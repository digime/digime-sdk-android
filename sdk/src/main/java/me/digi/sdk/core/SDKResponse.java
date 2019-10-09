/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core;

import retrofit2.Response;

public class SDKResponse <T> {
    public final Response response;
    public final T body;

    public SDKResponse(T body, Response response) {
        this.body = body;
        this.response = response;
    }
}
