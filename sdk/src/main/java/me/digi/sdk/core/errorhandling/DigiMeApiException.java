/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.errorhandling;

import android.text.TextUtils;

import com.google.gson.Gson;

import me.digi.sdk.core.entities.ErrorResponse;
import me.digi.sdk.core.entities.LegacyError;
import me.digi.sdk.core.entities.ServerError;

import okhttp3.Headers;
import okhttp3.HttpUrl;

import retrofit2.Response;

@SuppressWarnings("WeakerAccess")
public class DigiMeApiException extends SDKException {

    private final ServerError concreteError;
    private final Response response;

    private static final String HEADER_ERROR_CODE = "X-Error-Code";
    private static final String HEADER_ERROR_MESSAGE = "X-Error-Message";
    private static final Class[] CONCRETE_ERROR_CLASSES = {ErrorResponse.class, LegacyError.class};

    public DigiMeApiException(Response response) {
        this(response, readResponse(response), response.code());
    }

    private DigiMeApiException(Response response, ServerError concreteError,
                               int responseCode) {
        super(messageForCode(response.raw().request().url(), concreteError, responseCode));
        this.concreteError = concreteError;
        this.response = response;
        this.code = responseCode;
    }

    public String getErrorString() {
        return concreteError == null ? null : concreteError.message();
    }

    public Response getResponse() {
        return response;
    }

    private static ServerError readResponse(Response response) {
        ServerError finalError = null;
        try {
            @SuppressWarnings("ConstantConditions") final String body = response.errorBody().source().buffer().clone().readUtf8();
            if (!TextUtils.isEmpty(body)) {
                finalError = parseResponseBody(body, response.code());
            }
            if (finalError != null) {
                return finalError;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response.headers().get(HEADER_ERROR_CODE) != null) {
            finalError = parseHeaders(response.headers(), response.code());
        }

        return finalError == null ? new ErrorResponse("Request failed", response.message(), "", response.code()) : finalError;
    }

    private static ServerError parseResponseBody(String body, int code) {
        ServerError parsedError = null;
        final Gson gson = new Gson();
        for (Class<ServerError> klazz : CONCRETE_ERROR_CLASSES) {
            try {
                parsedError = gson.fromJson(body, klazz);
                parsedError.setCode(code);
            } catch (Exception e) {
                parsedError = null;
            }
            if (parsedError != null) {
                break;
            }
        }
        return parsedError;
    }

    private static ServerError parseHeaders(Headers headers, int code) {
        return new ErrorResponse(headers.get(HEADER_ERROR_CODE), headers.get(HEADER_ERROR_MESSAGE), "", code);
    }

    private static String messageForCode(HttpUrl requestURL, ServerError error, int code) {
        String reason = "error code";
        if (error != null) {
            APIError apiError = APIError.errorFromCode(error.errorCode());
            if (apiError != APIError.UNKNOWN)
                return apiError.userMessage;

            if (error.message() != null) {
                reason = error.message();
            } else if (error.errorCode() != null) {
                reason = error.errorCode();
            }
        }
        return String.format(requestURL.encodedPath() + " unsuccessful - %s (%s).", reason, String.valueOf(code));
    }
}
