/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.config.NetworkConfig;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Adapts a {@link Call} with response type {@code R} into a {@link ConfigurableCall}. Instances are
 * created by {@code CallConfigAdapterFactory a factory} which is
 * {@linkplain Retrofit.Builder#addCallAdapterFactory(CallAdapter.Factory) installed} into the {@link Retrofit}
 * instance and configured with {@link CallConfig} annotation.
 */
public class CallConfigAdapterFactory extends CallAdapter.Factory {
    private final ScheduledExecutorService callbackExecutor;

    private CallConfigAdapterFactory() {
        callbackExecutor = Executors.newScheduledThreadPool(1);
    }

    public static CallConfigAdapterFactory create() {
        return new CallConfigAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(final Type returnType, Annotation[] annotations, Retrofit retrofit) {
        boolean hasConfig = false;
        boolean shouldRetry = false;
        boolean hasRetryCodes = false;
        NetworkConfig config = null;
        // Iterate through all Call annotations and extract configuration from CallConfig.
        for (Annotation annotation : annotations) {
            if (annotation instanceof CallConfig) {
                hasConfig = true;
                CallConfig ant = ((CallConfig) annotation);
                boolean expBackoff = DigiMeClient.retryWithExponentialBackoff ? ant.doExponentialBackoff() : DigiMeClient.retryWithExponentialBackoff;
                int retryCount = DigiMeClient.maxRetryCount == 0 ? ant.retryCount() : DigiMeClient.maxRetryCount;
                shouldRetry = ant.shouldRetry();

                config = new NetworkConfig(retryCount, DigiMeClient.minRetryPeriod, ant.retryOnResponseCode(), expBackoff, ant.retriedExceptions());
                hasRetryCodes = ant.retryOnResponseCode().length > 0;
            }
        }

        /* Currently Call should be adapted (and therefore retried if it is annotated with a configuration, retiries are enabled globally,
           and retries are enabled in annotation OR if annotation specifies distinct HTTP codes to retry on. For the latter case proxying is done
           even if retries are disabled globally, since the Call can analyze the response code only after the chain has completed.
        */
        final boolean shouldRetryCall = ((hasConfig && DigiMeClient.retryOnFail) && shouldRetry) || hasRetryCodes;
        final NetworkConfig callConfigWrapper = config;
        // Fetch the next Adapter which will serve as a factory to create a base Call for us, therefore completing the chain
        //noinspection unchecked
        final CallAdapter<Object, Call<?>> delegate = (CallAdapter<Object, Call<?>>)retrofit.nextCallAdapter(this, returnType, annotations);
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return delegate.responseType();
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                // Currently we return a ConfigurableCall only if a call should be retried, otherwise adapt the original
                //noinspection unchecked
                return (Call<Object>) delegate.adapt(shouldRetryCall ? new ConfigurableCall<>(call, callbackExecutor, callConfigWrapper) : call);
            }
        };
    }

    /**
     * ConfigurableCall proxies the original Call and passes the configuration down the line to the callbacks
     */
    private static final class ConfigurableCall<T> implements Call<T> {
        private final Call<T> proxiedCall;
        private final ScheduledExecutorService callbackExecutor;
        private final NetworkConfig networkConfig;

        ConfigurableCall(Call<T> delegate, ScheduledExecutorService executor, NetworkConfig config) {
            proxiedCall = delegate;
            callbackExecutor = executor;
            networkConfig = config;
        }

        @Override
        public Response<T> execute() throws IOException {
            return proxiedCall.execute();
        }

        /**
         * Enques the {@linkplain ConfigurableCall#proxiedCall} async and proxies it through {@link ProxiedCallback}
         *
         * @param callback Original {@link Callback} that {@link ProxiedCallback} will proxy to
         */
        @Override
        public void enqueue(Callback<T> callback) {
            proxiedCall.enqueue(new ProxiedCallback<>(proxiedCall, callback, callbackExecutor, networkConfig));
        }

        @Override
        public void cancel() {
            proxiedCall.cancel();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override public Call<T> clone() {
            return new ConfigurableCall<>(proxiedCall.clone(), callbackExecutor, networkConfig);
        }

        @Override public boolean isExecuted() {
            return proxiedCall.isExecuted();
        }

        @Override public boolean isCanceled() {
            return proxiedCall.isCanceled();
        }

        @Override public Request request() {
            return proxiedCall.request();
        }
    }
}
