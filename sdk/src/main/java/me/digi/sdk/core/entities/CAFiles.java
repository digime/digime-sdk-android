/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CAFiles {

    @SerializedName("fileList")
    public List<String> fileIds;
}
