/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service;

import me.digi.examples.ca_no_sdk.service.models.DataGetEncryptedResponse;
import me.digi.examples.ca_no_sdk.service.models.DataGetResponse;
import me.digi.examples.ca_no_sdk.service.models.SessionKeyCreateResponse;
import me.digi.examples.ca_no_sdk.service.models.SessionTokenBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PermissionService {

    @Headers({
        "Content-type: application/json",
        "Cache-Control: no-cache",
        "Accept: application/json"
    })
    @POST("v1/permission-access/session")
    Call<SessionKeyCreateResponse> getSessionToken(@Body SessionTokenBody sessionTokenBody);

    @Headers({
        "Content-type: application/json",
        "Cache-Control: no-cache",
        "Accept: application/json"
    })
    @GET("v1/permission-access/query/{sessionKey}")
    Call<DataGetResponse> listDataFiles(@Path("sessionKey") String sessionKey);

    @Headers({
        "Content-type: application/json",
        "Cache-Control: no-cache",
        "Accept: application/json"
    })
    @GET("v1/permission-access/query/{sessionKey}/{fileName}")
    Call<DataGetEncryptedResponse> getDataFile(@Path("sessionKey") String sessionKey, @Path("fileName") String fileName);

    @Headers({
            "Content-type: application/json",
            "Cache-Control: no-cache",
            "Accept: application/json"
    })
    @GET("v1/permission-access/query/{sessionKey}/{fileName}")
    Call<DataGetResponse> getDataFileUnencrypted(@Path("sessionKey") String sessionKey, @Path("fileName") String fileName);
}
