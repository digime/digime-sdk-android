/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.session;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CASessionManager implements SessionManager<CASession> {

    private final ConcurrentHashMap<String, CASession> sessions;
    private final AtomicReference<CASession> currentSessionRef;
    private static final String MANAGER_NAME = "defaultCASessionManager";

    public final ListenerDispatch dispatch = new ListenerDispatch();

    public CASessionManager() {
        this.sessions = new ConcurrentHashMap<>(1);
        this.currentSessionRef = new AtomicReference<>();
    }

    @Override
    public String getManagerName() {
        return MANAGER_NAME;
    }

    @Override
    public CASession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        final CASession sess = sessions.get(sessionId);
        if (sess == null) {
            return null;
        } else {
            return sess;
        }
    }

    @Override
    public void addListener(SessionListener listener) {
        this.dispatch.addSessionListener(listener);
    }

    @Override
    public void removeListener(SessionListener listener) {
        this.dispatch.removeSessionListener(listener);
    }

    @Override
    public CASession getCurrentSession() {
        return currentSessionRef.get();
    }

    @Override
    public void setCurrentSession(CASession session) {
        if (session == null) {
            throw new IllegalArgumentException("Must set a non-null session!");
        }
        sessions.put(session.getId(), session);

        final CASession currentSession = currentSessionRef.get();
        synchronized (this) {
            if (currentSessionRef.compareAndSet(currentSession, session)) {
                dispatch.currentSessionChanged(currentSession, session);
            }
        }
    }

    @Override
    public void clearCurrentSession() {
        if (currentSessionRef.get() != null) {
            invalidateSession(currentSessionRef.get().getId());
        }
    }

    @Override
    public void setSession(CASession session) {
        if (session == null) {
            throw new IllegalArgumentException("Must set a non-null session!");
        }
        sessions.put(session.getId(), session);

        final CASession currentSession = currentSessionRef.get();
        if (currentSession == null || currentSession.getId().equals(session.getId()) || currentSession.getSessionKey().equals(session.getSessionKey())) {
            synchronized (this) {
                if (currentSessionRef.compareAndSet(currentSession, session)) {
                    dispatch.currentSessionChanged(currentSession, session);
                }
            }
        }
    }

    @Override
    public CASession invalidateSession(String id) {
        if (currentSessionRef.get() != null && currentSessionRef.get().getId().equals(id)) {
            synchronized (this) {
                currentSessionRef.set(null);
            }
        }
        CASession sess = sessions.remove(id);
        if (sess != null) {
            dispatch.sessionDestroyed(sess, SessionListener.DestroyedReason.INVALIDATED);
        }

        return sess;
    }

    @Override
    public Map<String, CASession> getSessions() {
        return Collections.unmodifiableMap(sessions);
    }
}
