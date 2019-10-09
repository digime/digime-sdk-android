/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.ipc;

import android.app.Activity;
import me.digi.sdk.core.DigiMeBaseAuthManager;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.session.SessionResult;

public interface AuthorizationResolver {
    <T extends SessionResult> void resolveAuthFlow(DigiMeBaseAuthManager authManager, Activity activity, SDKCallback<T> authCallback);
    <T extends SessionResult> void clientResolved(SDKCallback<T> authCallback);
    void stop();
    void overrideSessionCreation(boolean shouldOverride);
}
