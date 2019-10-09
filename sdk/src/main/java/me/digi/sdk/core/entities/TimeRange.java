/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/** Timestamps in UTC seconds */
public class TimeRange implements Serializable {

    @SerializedName("from")
    @Nullable
    public Long from;

    @SerializedName("to")
    @Nullable
    public Long priorTo;

    @SerializedName("last")
    @Nullable
    public String last;

    private TimeRange(@Nullable Long from, @Nullable Long priorTo, @Nullable String last) {
        this.from = from;
        this.priorTo = priorTo;
        this.last = last;
    }

    public static TimeRange from(@NonNull Long from) {
        return new TimeRange(from, null, null);
    }

    public static TimeRange priorTo(@NonNull Long priorTo) {
        return new TimeRange(null, priorTo, null);
    }

    public static TimeRange fromTo(@NonNull Long from, @NonNull Long to) {
        return new TimeRange(from, to, null);
    }

    public static TimeRange last(int x, @NonNull Unit unit) {
        return new TimeRange(null, null, String.valueOf(x)+unit.id);
    }

    public enum Unit {
        DAY("d"),
        MONTH("m"),
        YEAR("y");

        public final String id;

        Unit(String id) {
            this.id = id;
        }
    }
}
