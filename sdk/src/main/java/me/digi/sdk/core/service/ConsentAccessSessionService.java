/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.service;

import me.digi.sdk.core.CAContract;

import me.digi.sdk.core.internal.network.CallConfig;
import me.digi.sdk.core.session.CASession;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ConsentAccessSessionService {
    @CallConfig(shouldRetry = false)
    @Headers({
            "Content-type: application/json",
            "Cache-Control: no-cache",
            "Accept: application/json"
    })
    @POST("v1/permission-access/session")
    Call<CASession> getSessionToken(@Body CAContract contract);
}
