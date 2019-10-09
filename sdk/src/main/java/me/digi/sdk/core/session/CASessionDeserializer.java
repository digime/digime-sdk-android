/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.session;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import me.digi.sdk.core.DigiMeClient;

public class CASessionDeserializer implements JsonDeserializer<CASession> {
    public CASession deserialize(JsonElement json, Type typeOfT,
                             JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return new CASession();
        }

        final JsonObject obj = json.getAsJsonObject();
        String sessionKey;
        String sessionExchangeToken;
        long expiry;
        try {
            sessionKey = context.deserialize(obj.get("sessionKey"), String.class);
            sessionExchangeToken = context.deserialize(obj.get("sessionExchangeToken"), String.class);
            expiry = context.deserialize(obj.get("expiry"), long.class);
        } catch (Exception ex) {
            throw new JsonParseException("Wrong format retrieved from session object");
        }

        return new CASession(sessionKey, expiry, sessionKey,  (CASessionManager) DigiMeClient.getInstance().getSessionManager(), sessionExchangeToken);
    }
}

