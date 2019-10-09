package me.digi.sdk.core.MockEntities;

import org.mockito.Mock;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MockChain implements Interceptor.Chain {

    private Request request;
    private Response response;

    public MockChain(String requestUrl, Response response) {
        this.request = new Request.Builder().url(requestUrl).build();
        this.response = response;
    }

    @Override
    public Call call() {
        return null;
    }

    @Override
    public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public Connection connection() {
        return null;
    }
    @Override
    public int readTimeoutMillis() {
        return 0;
    }

    @Override
    public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public int connectTimeoutMillis() {
        return 0;
    }

    @Override
    public int writeTimeoutMillis() {
        return 0;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) {
        return response;
    }
}
