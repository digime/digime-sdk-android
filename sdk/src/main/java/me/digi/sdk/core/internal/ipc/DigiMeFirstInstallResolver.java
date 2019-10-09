/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.ipc;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import me.digi.sdk.core.DigiMeBaseAuthManager;
import me.digi.sdk.core.DigiMeConsentAccessAuthManager;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.session.SessionResult;

public class DigiMeFirstInstallResolver implements AuthorizationResolver {
    private static final int TIMEOUT_MINUTES = 10;
    private static final Handler mainLoopHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutHandler;
    private volatile DigiMeBaseAuthManager authorizationManager;

    public DigiMeFirstInstallResolver() {
        timeoutHandler = new Runnable() {
            @Override
            public void run() {
                authorizationManager.cancelOngoingAuthorization();
            }
        };
    }

    @Override
    public <T extends SessionResult> void resolveAuthFlow(DigiMeBaseAuthManager authManager, Activity activity, SDKCallback<T> authCallback) {
        InstallReceiver.registerForMessages();
        authorizationManager = authManager;
        authManager.beginDeferredAuthorization(activity, authCallback);
        mainLoopHandler.postDelayed(timeoutHandler, TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES));
    }

    @Override
    public <T extends SessionResult> void clientResolved(SDKCallback<T> authCallback) {
        clearTimeoutHandler();
        DigiMeClient.getInstance().createSession(authCallback, authorizationManager.getScope());
    }

    @Override
    public void stop() {
        clearTimeoutHandler();
        InstallReceiver.unregisterReceiver();
    }

    @Override
    public void overrideSessionCreation(boolean shouldOverride) { }

    private void clearTimeoutHandler() {
        try {
            mainLoopHandler.removeCallbacks(timeoutHandler);
        } catch (Exception ignored) { }
    }
}
