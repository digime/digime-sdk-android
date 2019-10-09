/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.session;

interface Session {

    String getId();

    boolean isValid();

    void requestCompleted();

    long getCreatedTime();

    long getLastAccessedTime();

    void invalidate();

    SessionManager getSessionManager();

    String changeSessionId(final String id);
}
