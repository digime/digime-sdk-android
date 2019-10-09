/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.provider;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import me.digi.sdk.core.BuildConfig;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.internal.CAExtractContentInterceptor;
import me.digi.sdk.core.DigiMeSDKVersion;
import me.digi.sdk.core.config.ApiConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.digi.sdk.crypto.CAKeyStore;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpProvider {

    private static final String SDK_USER_AGENT = "digi.me.sdk";

    public static OkHttpClient client(CertificatePinner certPinner, ApiConfig config) {
        return attachInterceptors(providerBuilder(certPinner), null, config)
                .build();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static OkHttpClient client(boolean attach, CertificatePinner certPinner, CAKeyStore keyStore, ApiConfig config) {
        if (!attach) {
            return providerBuilder(certPinner).build();
        }
        return attachInterceptors(providerBuilder(certPinner), keyStore, config)
                .build();
    }

    public static OkHttpClient client(
            OkHttpClient client,
            CertificatePinner certPinner,
            ApiConfig config) {

        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return attachInterceptors(client.newBuilder(), null, config)
                .connectionSpecs(defaultConnectionSpec())
                .certificatePinner(certPinner)
                .build();
    }

    private static OkHttpClient.Builder providerBuilder(CertificatePinner certPinner) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (certPinner != null) {
            builder.connectionSpecs(defaultConnectionSpec())
                    .certificatePinner(certPinner);
        }
        return builder;
    }

    private static List<ConnectionSpec> defaultConnectionSpec() {
        return Collections.singletonList(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build());
    }

    private static OkHttpClient.Builder attachInterceptors(OkHttpClient.Builder builder, CAKeyStore keyStore, final ApiConfig apiConfig) {

        if (BuildConfig.LOG_REQUESTS) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(logging);
        }
        builder.addInterceptor(new CAExtractContentInterceptor(keyStore != null ? keyStore : DigiMeClient.getDefaultKeyLoader().getStore()));
        return setDefaultTimeout(builder).addInterceptor(new Interceptor() {
                          @Override
                          public Response intercept(@NonNull Chain chain) throws IOException {
                              final Request request = chain.request().newBuilder()
                                      .header("User-Agent", apiConfig.userAgentString(SDK_USER_AGENT, DigiMeSDKVersion.VERSION))
                                      .build();
                              return chain.proceed(request);
                          }
                      });
    }

    private static OkHttpClient.Builder setDefaultTimeout(OkHttpClient.Builder builder) {
        return builder.connectTimeout(DigiMeClient.globalConnectTimeout, TimeUnit.SECONDS)
                .readTimeout(DigiMeClient.globalReadWriteTimeout, TimeUnit.SECONDS)
                .writeTimeout(DigiMeClient.globalReadWriteTimeout, TimeUnit.SECONDS);
    }
}
