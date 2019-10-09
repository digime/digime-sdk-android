/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.config;

import org.junit.Test;

import me.digi.sdk.core.BuildConfig;
import okhttp3.HttpUrl;

import static org.junit.Assert.*;

public class ApiConfigTest {
    @Test
    public void test_standard_api_config() throws Exception {
        ApiConfig config = new ApiConfig(HttpUrl.parse("https://api.consentaccess.sandboxdigi.me/something"));

        assertEquals("api.consentaccess.sandboxdigi.me", config.getHost());
        assertEquals("https://api.consentaccess.sandboxdigi.me/something", config.getUrl());
    }

}
