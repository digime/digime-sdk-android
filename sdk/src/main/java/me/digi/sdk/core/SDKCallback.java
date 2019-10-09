/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core;

import android.support.annotation.NonNull;

import java.net.SocketTimeoutException;

import me.digi.sdk.core.errorhandling.DigiMeApiException;
import me.digi.sdk.core.errorhandling.SDKException;
import retrofit2.Call;
import retrofit2.Response;

public abstract class SDKCallback<T> implements retrofit2.Callback<T> {
    public static final int TIMEOUT_ERROR = 507;

    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response){
        if (response.isSuccessful()) {
            succeeded(new SDKResponse<>(response.body(), response));
        } else {
            failed(new DigiMeApiException(response));
        }
    }

    @Override
    public final void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        failed((t instanceof SocketTimeoutException) ? new SDKException("Connection timeout", t, TIMEOUT_ERROR) :  new SDKException("Request Failure", t));
    }

    public abstract void succeeded(SDKResponse<T> result);
    public abstract void failed(SDKException exception);
}
