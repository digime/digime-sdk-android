/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.ipc;

import android.app.Activity;

import me.digi.sdk.core.DigiMeBaseAuthManager;
import me.digi.sdk.core.DigiMeConsentAccessAuthManager;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.DigiMePostboxAuthManager;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.session.SessionResult;

public class DigiMeDirectResolver implements AuthorizationResolver {
    private boolean shouldOverride = false;

    @Override
    public <T extends SessionResult> void resolveAuthFlow(DigiMeBaseAuthManager authManager, Activity activity, SDKCallback<T> authCallback) {
        if (!shouldOverride) {
            if (authManager instanceof DigiMePostboxAuthManager)
                DigiMeClient.getInstance().createPostboxSession(authCallback);
            else
                DigiMeClient.getInstance().createSession(authCallback, authManager.getScope());
        } else {
            authManager.beginAuthorization(activity, authCallback);
        }
    }

    @Override
    public <T extends SessionResult> void clientResolved(SDKCallback<T> authCallback) { }

    @Override
    public void stop() { }

    @Override
    public void overrideSessionCreation(boolean shouldOverride) {
        this.shouldOverride = shouldOverride;
    }
}
