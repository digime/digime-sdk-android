/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import me.digi.sdk.core.session.CASession;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SessionPathInterceptor implements Interceptor {
    private final CASession currentSession;

    public SessionPathInterceptor(CASession session) {
        this.currentSession = session;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request originalRequest = chain.request();
        HttpUrl.Builder urlBuilder = originalRequest.url().newBuilder();
        List<String> pathSegments = originalRequest.url().pathSegments();
        String sessionKey = String.format("{%s}", "sessionKey");

        for (int i = 0; i < pathSegments.size(); i++) {
            if (sessionKey.equalsIgnoreCase(pathSegments.get(i))) {
                urlBuilder.setPathSegment(i, currentSession.getSessionKey());
            }
        }
        Request request = originalRequest.newBuilder()
                .url(urlBuilder.build())
                .build();
        return chain.proceed(request);
    }
}
