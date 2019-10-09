/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import me.digi.sdk.core.entities.DataRequest;
import me.digi.sdk.core.internal.AuthorizationException;
import me.digi.sdk.core.internal.ipc.AuthorizationResolver;
import me.digi.sdk.core.internal.quark.QuarkBrowserActivity;
import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.session.SessionManager;
import me.digi.sdk.core.session.SessionResult;

@SuppressWarnings("WeakerAccess")
public abstract class DigiMeBaseAuthManager<T extends SessionResult> {

    public static final String DIGI_ME_PACKAGE_ID = "me.digi.app3";

    protected static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
    protected static final String KEY_APP_ID = "KEY_APP_ID";
    protected static final String KEY_APP_NAME = "KEY_APP_NAME";
    protected static final String DEFAULT_UNKNOWN_APP_NAME = "Android SDK App";
    protected static final String KEY_SDK_VERSION = "SDK_VERSION";

    protected WeakReference<Activity> authActivity;
    protected AuthorizationResolver resolver;

    protected static final AtomicReference<DigiMeAuthorizationState> authInProgress = new AtomicReference<>(DigiMeAuthorizationState.IDLE);
    protected SDKCallback<T> callback;

    protected final String appId;
    protected String appName;
    protected DataRequest scope;
    protected final CASession session;
    protected final SessionManager<CASession> sManager;

    public DigiMeBaseAuthManager() {
        this(DigiMeClient.getApplicationId(), DigiMeClient.getApplicationName(), DigiMeClient.getInstance().getSessionManager());
    }

    public DigiMeBaseAuthManager(String applicationId, String applicationName, SessionManager<CASession> manager) {
        this.appId = applicationId;
        this.appName = applicationName;
        this.sManager = manager;
        this.session = null;
        verifyAppName();
    }

    abstract public int getRequestCode();

    abstract protected Intent createAppIntent(CASession intentSession);

    abstract protected void handleSuccess(@Nullable Intent data);

    abstract protected AuthorizationResolver createResolver(Activity activity);

    public DataRequest getScope() {
        return scope;
    }

    public void setScope(DataRequest scope) {
        this.scope = scope;
    }

    @SuppressWarnings("UnusedParameters")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getRequestCode()) {
            if (resultCode == Activity.RESULT_OK) {
                handleSuccess(data);
            } else {
                callback.failed(new AuthorizationException("Access denied", extractSession(), AuthorizationException.Reason.ACCESS_DENIED));
            }
            cancelOngoingAuthorization();
        } else {
            callback.failed(new AuthorizationException("Access denied", null, AuthorizationException.Reason.WRONG_CODE));
        }
    }

    public void resolveAuthorizationPath(Activity activity, SDKCallback<T> callback, boolean overrideSessionCreate) {
        resolver = createResolver(activity);
        resolver.overrideSessionCreation(overrideSessionCreate);
        resolver.resolveAuthFlow(this, activity, callback);
    }

    public void beginAuthorization(Activity activity, SDKCallback<T> callback) {
        if (activity == null) {
            throw new IllegalArgumentException("Must set the activity to start the flow.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must set the callback.");
        }
        if (!activity.isFinishing()) {
            authActivity = new WeakReference<>(activity);
            prepareRequest(activity, callback);
        }
    }

    public void beginDeferredAuthorization(Activity activity, SDKCallback<T> callback) {
        if (activity == null) {
            throw new IllegalArgumentException("Must set the activity to start the flow.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must set the callback.");
        }
        if (!activity.isFinishing()) {
            if (!markInDeferredProgress()) {
                callback.failed(new AuthorizationException("Authorization already in progress! Explicitly call cancelOngoingAuthorization() to explictly restart authorization."));
                cancelOngoingAuthorization();
                return;
            }
            this.authActivity = new WeakReference<>(activity);
            this.callback = callback;
            startInstallDigiMeFlow(activity);
        } else {
            callback.failed(new AuthorizationException("Activity in finished state!"));
            cancelOngoingAuthorization();
        }
    }

    public void protocolResolved() {
        if (isInProgress() || isDeferred()) {
            resolver.clientResolved(callback);
        }
    }

    public void cancelOngoingAuthorization() {
        if (isDeferred() && callback != null) {
            callback.failed(new AuthorizationException("Authorization timed out while waiting for native client!"));
        }
        if (resolver != null) {
            resolver.stop();
        }
        clearProgress();
        resolver = null;
        this.authActivity = null;
        this.callback = null;
    }

    public abstract boolean canHandleAuthImmediately(Activity activity);

    public boolean nativeClientAvailable(Activity activity) {
        Intent appIntent = createAppIntent(null);
        return verifyIntentCanBeHandled(appIntent, activity.getPackageManager());
    }

    private void prepareRequest(Activity activity, SDKCallback<T> callback) {
        CASession requestSession = extractSession();
        if (requestSession == null) {
            throw new NullPointerException("Session is null.");
        }
        if (!sendRequest(requestSession, activity, callback)) {
            callback.failed(new AuthorizationException("Authorization is already in progress.", requestSession, AuthorizationException.Reason.IN_PROGRESS));
        }
    }

    private boolean sendRequest(CASession session, Activity activity, SDKCallback<T> callback) {
        if (!markInProgress()) {
            return false;
        }
        this.callback = callback;

        if (this instanceof DigiMeGuestCAAuthManager) {
            QuarkBrowserActivity.startForResult(activity, session.sessionExchangeToken, getRequestCode());
            return true;
        }

        Intent appIntent = createAppIntent(session);
        if (verifyIntentCanBeHandled(appIntent, activity.getPackageManager())) {
            activity.startActivityForResult(appIntent, getRequestCode());
            return true;
        } else {
            startInstallDigiMeFlow(activity);
            return false;
        }
    }

    public CASession extractSession() {
        CASession requestSession = session;
        if (requestSession == null && sManager != null) {
            requestSession = sManager.getCurrentSession();
        }
        return requestSession;
    }

    private void verifyAppName() {
        if (TextUtils.isEmpty(appName)) {
            final PackageManager pm = DigiMeClient.getApplicationContext().getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(DigiMeClient.getApplicationContext().getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            this.appName = ai != null ? (String) pm.getApplicationLabel(ai) : DEFAULT_UNKNOWN_APP_NAME;
        }
    }

    @SuppressWarnings("PackageManagerGetSignatures")
    private boolean verifyIntentCanBeHandled(Intent intent, PackageManager packageManager) {
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
        if (resolveInfo == null) {
            return false;
        }
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() == 0) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(DIGI_ME_PACKAGE_ID, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        //TODO reenable it! DIGI-3922
        /*
        for (Signature s : packageInfo.signatures) {
            if (!PackageSignatures.matchesSignature(s.toCharsString())) {
                return false;
            }
        }
        */
        return true;
    }

    protected void startInstallDigiMeFlow(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DIGI_ME_PACKAGE_ID)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + DIGI_ME_PACKAGE_ID)));
        }
        Toast.makeText(activity, "digi.me not found.", Toast.LENGTH_LONG).show();
    }

    private boolean markInProgress() {
        boolean result = false;
        if (isInProgress()) {
            Log.d(DigiMeClient.TAG, "Authorization is already in progress.");
        } else {
            authInProgress.set(DigiMeAuthorizationState.IN_PROGRESS);
            result = true;
        }
        return result;
    }

    private boolean markInDeferredProgress() {
        boolean result = false;
        if (isInProgress() || isDeferred()) {
            Log.d(DigiMeClient.TAG, "Authorization is already in progress.");
        } else {
            authInProgress.set(DigiMeAuthorizationState.DEFERRED);
            result = true;
        }
        return result;
    }

    private void clearProgress() {
        authInProgress.set(DigiMeAuthorizationState.IDLE);
    }

    public boolean isInProgress() {
        return authInProgress.get() != null && authInProgress.get() == DigiMeAuthorizationState.IN_PROGRESS;
    }

    public boolean isDeferred() {
        return authInProgress.get() != null && authInProgress.get() == DigiMeAuthorizationState.DEFERRED;
    }

    public enum DigiMeAuthorizationState {
        IDLE,
        IN_PROGRESS,
        DEFERRED
    }

}
