/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Retrofit Call configuration annotation. Allows to change many configuration options on a call, but primary use is for automatic retries.
 *
 * Works in tandem with {@code CallConfigAdapterFactory} which spawns a proxy callback {@code ProxiedCallback}.
 * Call will retry by default on HTTP codes 5xx, unless other codes are specified in {@code CallConfig#retryOnResponseCode}
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface CallConfig {
    /**
     * Property defines whether a call should be retried
     */
    boolean shouldRetry() default false;

    /**
     * If set call will be retried for responses with specified code, even if retries are disabled globally
     */
    int[] retryOnResponseCode() default {};

    /**
     * How many times to retry max
     */
    int retryCount() default 2;

    /**
     * Should the call use exponential backoff policy when retrying
     */
    boolean doExponentialBackoff() default true;

    /**
     * Which internal exceptions to retry
     */
    Class<?>[] retriedExceptions() default {};
}
