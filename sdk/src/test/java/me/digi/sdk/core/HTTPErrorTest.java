/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.session.CASession;
import me.digi.sdk.crypto.CAKeyStore;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HTTPErrorTest {
    private static final String APP_ID_ERROR_MESSAGE = "This app is no longer valid for Consent Access";
    private MockWebServer server;

    @Before
    public void startUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @Test
    public void test_support_for_error_response_legacy() throws InterruptedException {
        final String response = "{\"error\":\"InvalidConsentAccessApplication\",\"message\":\"Application is not valid for Consent Access\",\"code\":12345}";
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse().setBody(response)
                .setResponseCode(403);
        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(server.url("/"), wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals(APP_ID_ERROR_MESSAGE, wrapper.result);
    }

    @Test
    public void test_support_for_error_response_v2() throws InterruptedException {
        final String response = "{\"error\":{\"code\":\"InvalidConsentAccessApplication\",\"message\":\"Application is not valid for Consent Access\",\"reference\":\"GUID-GUID-GUID\"}}";
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse().setBody(response)
                                                .setResponseCode(403);
        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(server.url("/"), wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals(APP_ID_ERROR_MESSAGE, wrapper.result);
    }

    @Test
    public void test_support_for_error_response_in_headers() throws IOException, InterruptedException {
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse()
                .setBody("")
                .addHeader("X-Error-Code", "InvalidConsentAccessApplication")
                .addHeader("X-Error-Message", "Application is not valid for Consent Access")
                .setResponseCode(403);

        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(server.url("/"), wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals(APP_ID_ERROR_MESSAGE, wrapper.result);
    }

    @Test
    public void test_non_appId_revoke_error_fallthrough() throws IOException, InterruptedException {
        final String response = "{\"error\":{\"code\":\"SomeOtherExceptionCode\",\"message\":\"Application is not valid for Consent Access\",\"reference\":\"GUID-GUID-GUID\"}}";
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse().setBody(response)
                .setResponseCode(403);

        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(server.url("/"), wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals("/v1/permission-access/session unsuccessful - Application is not valid for Consent Access (403).", wrapper.result);
    }

    @After
    public void shutdown() throws IOException {
        server.shutdown();
    }

    private CountDownLatch executeAsyncCallback(HttpUrl baseUrl, final ResultWrapper wrapper) {
        CAContract mockContract = new CAContract("dummyId", "me.digi.sdk.test");
        final CountDownLatch latch = new CountDownLatch(1);
        DigiMeAPIClient client = new DigiMeAPIClient(false, new CAKeyStore(""), new ApiConfig(baseUrl));
        //Emulate real pipeline asynchronously so that SDKCallback error processing can be tested
        client.sessionService().getSessionToken(mockContract).enqueue(new SDKCallback<CASession>() {
            @Override
            public void succeeded(SDKResponse<CASession> result) {
                wrapper.succeedIndicator = true;
                latch.countDown();
            }

            @Override
            public void failed(SDKException exception) {
                wrapper.succeedIndicator = false;
                wrapper.result = exception.getMessage();
                latch.countDown();
            }
        });
        return latch;
    }

    private class ResultWrapper {
        boolean succeedIndicator;
        String result;
    }

}
