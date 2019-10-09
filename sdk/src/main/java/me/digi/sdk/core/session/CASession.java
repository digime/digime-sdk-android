/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.session;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import me.digi.sdk.core.DigiMeClient;

public class CASession implements Session, SessionResult {

    public static final long DEFAULT_EXPIRY = 60000;
    private static final String INVALID_SESSION_KEY = "invalid_key";

    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("sessionExchangeToken")
    public String sessionExchangeToken;

    @SerializedName("expiry")
    public long expiry;

    final CASessionManager sessionManager;
    volatile long lastAccessed;
    final long creationTime;

    private String sessionId;
    private volatile boolean invalid = false;

    public CASession() {
        this(INVALID_SESSION_KEY, System.currentTimeMillis() + DEFAULT_EXPIRY, null, (CASessionManager) DigiMeClient.getInstance().getSessionManager(), INVALID_SESSION_KEY);
    }

    public CASession(String sessionKey, long expiry, String sessionId, CASessionManager sessionManager, String sessionExchangeToken) {

        if (sessionKey == null)
            throw new IllegalArgumentException("Valid session key must be provided.");

        if(sessionExchangeToken == null)
            throw new IllegalArgumentException("Valid session exchange token must be provided.");

        this.sessionKey = sessionKey;
        this.expiry = expiry;
        this.sessionManager = sessionManager;
        this.sessionId = sessionId;
        this.sessionExchangeToken = sessionExchangeToken;
        creationTime = lastAccessed = System.currentTimeMillis();
        if (sessionManager != null) {
            sessionManager.dispatch.sessionCreated(this);
        }
    }

    @NonNull
    public String getSessionKey() {
        return sessionKey;
    }

    public long getExpiry() {
        return expiry;
    }

    @Override
    public CASession session() {
        return this;
    }

    @NonNull
    @Override
    public String getId() {
        if (sessionId == null) {
            sessionId = sessionKey;
        }
        return sessionId;
    }

    @Override
    public boolean isValid() {
        return System.currentTimeMillis() <= expiry && !invalid;
    }

    @Override
    public void requestCompleted() {
        if (!invalid) {
            lastAccessed = System.currentTimeMillis();
        }
    }

    @Override
    public long getCreatedTime() {
        return creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessed;
    }

    @Override
    public void invalidate() {
        synchronized(CASession.this) {
            CASession sess = sessionManager.invalidateSession(sessionId);
            if (sess == null) {
                throw new IllegalStateException("Session already invalidated");
            }
        }
        invalid = true;
    }

    @Nullable
    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Nullable
    @Override
    public String changeSessionId(String id) {
        final String oldId = sessionId;
        if (oldId.equals(id)) {
            return sessionId;
        }
        this.sessionId = id;
        if(!invalid) {
            sessionManager.setSession(this);
        }
        sessionManager.invalidateSession(oldId);
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        final CASession session = (CASession) obj;
        return sessionKey.equals(session.sessionKey);
    }

    @Override
    public int hashCode() {
        return sessionKey.hashCode();
    }
}
