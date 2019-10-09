/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import android.support.annotation.NonNull;

import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.config.NetworkConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A proxy {@linkplain Callback} that controls enforcement of configuration rules from {@link CallConfigAdapterFactory}
 *
 * It takes in configuration options in {@link NetworkConfig}, analyzes response and on failures schedules a retry {@link Call}
 * if all configuration options align with the state of the {@linkplain #proxiedCall}. By default {@code ProxiedCallback} schedules
 * a retry with exponential backoff timer {@link BackOffTimer} if enabled otherwise it waits 100 ms.
 *
 * By default retries are scheduled in case of 5xx responses or if a {@link SocketTimeoutException} occurs
 *
 * If exit cases are reached (max retries reached, max interval reached, response was successful, ...)
 * {@code ProxiedCallback} calls back into registered callback (the one that was attached to proxied Call)
 */
public class ProxiedCallback<T> implements Callback<T> {
    private static final int SOCKET_TIMEOUT_MAX_ALLOWED = 120; //2 minutes cumulative wait time with retries

    private final BackOffTimer backOffTimer;
    private final Call<T> proxiedCall;
    private final Callback<T> registeredCallback;
    private final ScheduledExecutorService callbackExecutor;
    private final NetworkConfig networkConfig;
    private final int triesAlready;

    /**
     * Instantiates a new Proxied callback.
     *
     * @param call     {@link Call} to proxy
     * @param delegate Delegate callback, that receives the actual result
     * @param executor Scheduled executro to use for retry scheduling
     * @param config   {@link Call} configuration
     */
    ProxiedCallback(Call<T> call, Callback<T> delegate, ScheduledExecutorService executor, NetworkConfig config) {
        this(call, delegate, executor, config, 0);
    }

    private ProxiedCallback(Call<T> call, Callback<T> delegate, ScheduledExecutorService executor, NetworkConfig config, int retries) {
        this.proxiedCall = call;
        this.registeredCallback = delegate;
        this.callbackExecutor = executor;
        this.networkConfig = config;
        this.triesAlready = retries;
        this.backOffTimer = config.shouldPerformExponentialBackoff() ? new BackOffTimer((int)config.getMinDelay()) : null;
    }

    private ProxiedCallback(Call<T> call, Callback<T> delegate, ScheduledExecutorService executor, NetworkConfig config, int retries, BackOffTimer timer) {
        this.proxiedCall = call;
        this.registeredCallback = delegate;
        this.callbackExecutor = executor;
        this.networkConfig = config;
        this.triesAlready = retries;
        this.backOffTimer = timer;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!response.isSuccessful()) {
            long nextDelay = getBackoff();
            if (nextDelay != BackOffTimer.STOP && isRetryRequired(networkConfig, response.code()) && triesAlready < networkConfig.getMaxRetries()) {
                scheduleCall(nextDelay);
                return;
            }
        }
        registeredCallback.onResponse(call, response);
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        final long nextDelay = getBackoff();
        if (nextDelay == BackOffTimer.STOP || !isRetryRequired(networkConfig, t)) {
            registeredCallback.onFailure(call, t);
        } else if (triesAlready < networkConfig.getMaxRetries()) {
            scheduleCall(nextDelay);
        } else {
            registeredCallback.onFailure(call, t);
        }
    }

    private long getBackoff() {
        return backOffTimer != null ? backOffTimer.calculateNextBackOffMillis() : 100;
    }

    private boolean isRetryRequired(NetworkConfig config, int statusCode) {
        return (config.getAlwaysOnCodes() != null && configContainsCode(config, statusCode)) || (statusCode >= 500 && statusCode < 600);
    }

    private boolean isRetryRequired(NetworkConfig config, Throwable t) {
        // We allow retries on timeout only if cumualative wait time is less than 2 minutes
        if (t instanceof SocketTimeoutException && (DigiMeClient.globalConnectTimeout * config.getMaxRetries()) < SOCKET_TIMEOUT_MAX_ALLOWED) {
            return true;
        }
        for (Class<?> cls: config.getRetriedExceptions()) {
            if (t.getClass().isAssignableFrom(cls)) {
                return true;
            }
        }
        return false;
    }

    private boolean configContainsCode(NetworkConfig config, int statusCode) {
        if (config.getAlwaysOnCodes() == null || config.getAlwaysOnCodes().length == 0)  return false;
        for (int code: config.getAlwaysOnCodes()) {
            if (code == statusCode) return true;
        }
        return false;
    }

    private void scheduleCall(long delay) {
        callbackExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                final Call<T> call = proxiedCall.clone();
                call.enqueue(new ProxiedCallback<>(call, registeredCallback, callbackExecutor, networkConfig, triesAlready + 1, backOffTimer));
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
