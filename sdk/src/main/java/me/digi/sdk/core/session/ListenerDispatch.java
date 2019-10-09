/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.session;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
class ListenerDispatch {

    private final List<SessionListener> listeners = new CopyOnWriteArrayList<>();

    void addSessionListener(final SessionListener listener) {
        this.listeners.add(listener);
    }

    boolean removeSessionListener(final SessionListener listener) {
        return this.listeners.remove(listener);
    }

    public void clear() {
        this.listeners.clear();
    }

    public void sessionCreated(final Session session) {
        for (SessionListener listener : listeners) {
            listener.sessionCreated(session);
        }
    }

    void sessionDestroyed(final Session session, SessionListener.DestroyedReason reason) {
        List<SessionListener> listeners = new ArrayList<>(this.listeners);
        ListIterator<SessionListener> iterator = listeners.listIterator(listeners.size());
        while (iterator.hasPrevious()) {
            iterator.previous().sessionDestroyed(session, reason);
        }
    }

    void currentSessionChanged(final Session oldSession, final Session newSession) {
        for (SessionListener listener : listeners) {
            listener.currentSessionChanged(oldSession, newSession);
        }
    }

}
