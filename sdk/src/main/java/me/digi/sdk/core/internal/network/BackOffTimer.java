/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import java.io.IOException;

/**
 * {@code BackOffTimer} controls and computes the exponential backoff policy using
 * a randomization function that grows exponentially.
 *
 * <p>
 * To calculate backoff, {@link #calculateNextBackOffMillis()} uses the following formula:
 * </p>
 *
 * <pre>
 delay =
 retry_delay * (random with range [1 - rand_factor, 1 + rand_factor])
 * </pre>
 */
public class BackOffTimer {

    /**
     * Marker that denotes we've reached one of the maximums and retries should be aborted
     */
    static final long STOP = -100L;

    /**
     * Default minimal start delay interval
     */
    public static final int DEFAULT_MIN_INTERVAL = 500;
    /**
     * Default randomization factor when computing the policy
     */
    public static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5;
    /**
     * Default multiplier to apply to the backoff policy
     */
    public static final double DEFAULT_MULTIPLIER = 1.5;
    /**
     * Default max delay that can be set
     */
    public static final int DEFAULT_MAX_INTERVAL = 60000;
    /**
     * Default max elapsed delay across all retries
     */
    public static final int DEFAULT_MAX_ELAPSED_TIME = 800000;

    private int currentDelay;

    private final int initialInterval;
    private final double randomizationFactor;
    private final double multiplier;
    private final int maxInterval;
    //Nanosecond precision
    private long timerStart;

    private final int maxElapsedTime;

    public BackOffTimer() {
        initialInterval = DEFAULT_MIN_INTERVAL;
        randomizationFactor = DEFAULT_RANDOMIZATION_FACTOR;
        multiplier = DEFAULT_MULTIPLIER;
        maxInterval = DEFAULT_MAX_INTERVAL;
        maxElapsedTime = DEFAULT_MAX_ELAPSED_TIME;
        rewind();
    }

    /**
     * Instantiates a new Back off timer.
     *
     * @param minInterval starting base delay in milliseconds
     */
    public BackOffTimer(int minInterval) {
        initialInterval = minInterval;
        randomizationFactor = DEFAULT_RANDOMIZATION_FACTOR;
        multiplier = DEFAULT_MULTIPLIER;
        maxInterval = DEFAULT_MAX_INTERVAL;
        maxElapsedTime = DEFAULT_MAX_ELAPSED_TIME;
        rewind();
    }

    /**
     * Gets elapsed time.
     *
     * @return the elapsed time
     */
    public final long getElapsedTime() {
        return (System.nanoTime() - timerStart) / 1000000;
    }

    /**
     * Reset the timer
     */
    public final void rewind() {
        currentDelay = initialInterval;
        timerStart = System.nanoTime();
    }

    /**
     * Calculate next back off delay in milliseconds.
     *
     * @return Next delay in milliseconds or {@link #STOP} marker if elapsed time is above {@linkplain #DEFAULT_MAX_ELAPSED_TIME}
     */
    public long calculateNextBackOffMillis() {
        if (getElapsedTime() > maxElapsedTime) {
            return STOP;
        }
        int randomizedInterval = randomizeInterval(randomizationFactor, Math.random(), currentDelay);
        incrementDelay();
        return randomizedInterval;
    }

    private void incrementDelay() {
        if (currentDelay >= maxInterval / multiplier) {
            currentDelay = maxInterval;
        } else {
            currentDelay *= multiplier;
        }
    }

    private static int randomizeInterval(
            double randomizationFactor, double random, int currentIntervalMillis) {
        double delta = randomizationFactor * currentIntervalMillis;
        double minInterval = currentIntervalMillis - delta;
        double maxInterval = currentIntervalMillis + delta;

        return (int) (minInterval + (random * (maxInterval - minInterval + 1)));
    }

}
