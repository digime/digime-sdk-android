/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.session;

import java.util.Map;

public interface SessionManager<T extends Session> {

    String getManagerName();

    T getSession(final String sessionId);

    void addListener(final SessionListener listener);

    void removeListener(final SessionListener listener);

    T getCurrentSession();

    void setCurrentSession(T session);

    void clearCurrentSession();

    void setSession(T session);

    T invalidateSession(String id);

    Map<String, T> getSessions();
}
