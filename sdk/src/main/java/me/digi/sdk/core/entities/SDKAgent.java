/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core.entities;

import android.os.Build;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import me.digi.sdk.core.DigiMeSDKVersion;
import me.digi.sdk.core.config.ApiConfig;

// Validation spec: https://digi-me.atlassian.net/wiki/spaces/ENG/pages/394002569/CA+SDK+Compatibility
public class SDKAgent {

    @SerializedName("name")
    public final String platform;

    @SerializedName("version")
    public String version;

    // anything else that may be useful
    @SerializedName("meta")
    public JsonObject meta;

    public SDKAgent() {
        platform = "android";
        version = ApiConfig.parseVersion(DigiMeSDKVersion.VERSION);
        meta = createMeta();
    }

    private static JsonObject createMeta() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("device", Build.MODEL);
        jsonObject.addProperty("osVersion", Build.VERSION.RELEASE);
        return jsonObject;
    }

}
