/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.crypto.testmodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TestJFSFile {

    @SerializedName("createdDatecreateddate")
    public long createdDate;

    @SerializedName("dateModedatemode")
    public String mode;

    @SerializedName("jfsVersionjfsversion")
    public String version;

    @SerializedName("mapFilePathmapfilepath")
    public String mapFilePath;

    @SerializedName("msks")
    public List<String> msks;

    @SerializedName("updatedDateupdateddate")
    public long updateDate;

    @SerializedName("uuid")
    public int uuid;

}
