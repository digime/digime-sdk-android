/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core.entities;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

// Confluence ContractSchemav2-DataRequest
public class DataRequest implements Serializable {

    // Not yet supported
    /*@SerializedName("serviceGroups")
    public List<ServiceGroup> objectTypes;*/

    @SerializedName("timeRanges")
    public List<TimeRange> timeRanges;

    public DataRequest(@NonNull List<TimeRange> timeRanges) {
        this.timeRanges = timeRanges;
    }
}
