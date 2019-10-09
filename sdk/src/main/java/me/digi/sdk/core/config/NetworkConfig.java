/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.config;

import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class NetworkConfig {
    private int[] alwaysOnCodes;
    private int maxRetries;
    private long minDelay;
    private boolean doExponentialBackoff;
    private List<Class<?>> retriedExceptions;

    public NetworkConfig(int maxRetries, long minDelay) {
        this(maxRetries, minDelay, null, true, new Class[] {});
    }

    public NetworkConfig(int maxRetries, long minDelay, @Nullable int[] alwaysOnCodes, boolean doExponentialBackoff, Class<?>[] exceptions) {
        this.maxRetries = maxRetries;
        this.minDelay = minDelay;
        this.alwaysOnCodes = alwaysOnCodes;
        this.doExponentialBackoff = doExponentialBackoff;
        this.retriedExceptions = Arrays.asList(exceptions);
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getMinDelay() {
        return minDelay;
    }

    public int[] getAlwaysOnCodes() {
        return alwaysOnCodes;
    }

    public boolean shouldPerformExponentialBackoff() {
        return doExponentialBackoff;
    }

    public List<Class<?>> getRetriedExceptions() { return retriedExceptions; }

}
