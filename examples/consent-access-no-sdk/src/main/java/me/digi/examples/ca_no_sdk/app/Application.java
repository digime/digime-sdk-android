/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.net.Uri;

import me.digi.examples.ca_no_sdk.BuildConfig;
import me.digi.examples.ca_no_sdk.service.PermissionService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Application extends android.app.Application {
    private static final String PERMISSION_SERVICE_BASE_URL = BuildConfig.API_URL;

    private PermissionService permissionService;

    public synchronized PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = new Retrofit.Builder()
                .baseUrl(PERMISSION_SERVICE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build()
                .create(PermissionService.class);
        }
        return permissionService;
    }

    private OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .build();

        return new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES).readTimeout(50, TimeUnit.SECONDS)
            .connectionSpecs(Collections.singletonList(connectionSpec))
            .addInterceptor(logging)
            .certificatePinner(createPinner())
            .build();
    }

    private CertificatePinner createPinner() {
        String host = Uri.parse(PERMISSION_SERVICE_BASE_URL).getHost();
        return new CertificatePinner.Builder()
                .add(host, "sha256/FuXLwrAfrO4L3Cu03eXcXAH1BnnQRJeqy8ft+dVB4TI=")
                .add(host, "sha256/41Vcs2jOzcXdsDsbDt/nsNQRUZsYhCTPoeODK6VaWF0=")
                .add(host, "sha256/HC6oU3LGzhkwHionuDaZacaIbjwYaMT/Qc7bxWLyy8g=")
                .add(host, "sha256/2qix+QNHzGWG5nhEFNIMxPZ57YbgT0liSisVLERNzt8=")
                .add(host, "sha256/W8QTLPG35cP39gFmUjKLLKAlHrYmGxvHf5Zf+INBZzo=")
                .build();
    }
}
