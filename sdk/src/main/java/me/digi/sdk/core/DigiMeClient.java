/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.gson.JsonElement;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.entities.CAAccounts;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.entities.DataRequest;
import me.digi.sdk.core.entities.TimeRange;
import me.digi.sdk.core.errorhandling.DigiMeClientException;
import me.digi.sdk.core.errorhandling.DigiMeException;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.errorhandling.SDKValidationException;
import me.digi.sdk.core.internal.AuthorizationException;
import me.digi.sdk.core.internal.Util;
import me.digi.sdk.core.provider.KeyLoaderProvider;
import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.session.CASessionManager;
import me.digi.sdk.core.session.SessionManager;
import me.digi.sdk.core.session.SessionResult;
import okhttp3.OkHttpClient;


@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "SameParameterValue", "UnusedReturnValue", "StaticFieldLeak", "WeakerAccess"})
public final class DigiMeClient {
    static final String TAG = "DigiMeCore";

    private static volatile DigiMeClient singleton;

    private static volatile Executor coreExecutor;
    private static volatile String applicationId;
    private static volatile String applicationName;
    private static volatile String[] caContractIds;
    private static volatile String[] postboxContractIds;

    private static final boolean debugEnabled = BuildConfig.DEBUG;

    /**
     *   Connection timeout in seconds
     */
    public static int globalConnectTimeout = 25;

    /**
     *   Connection read/write IO timeout in seconds
     */
    public static int globalReadWriteTimeout = 30;

    /**
     *   Controls retries globally
     */
    public static boolean retryOnFail = true;

    /**
     *   Minimal delay to retry failed request
     */
    public static long minRetryPeriod = 500;

    /**
     *   Minimal delay to retry failed request
     */
    public static boolean retryWithExponentialBackoff = true;

    /**
     *   Maximum number of times to retry before failing. 0 uses per call defaults, >0 sets a global hard limit.
     */
    public static int maxRetryCount = 0;

    private static Context appContext;
    private static final Object SYNC = new Object();
    private static KeyLoaderProvider loaderProvider;

    //Predefined <meta-data> paths where the sdk looks for necessary items
    private static final String APPLICATION_ID_PATH = "me.digi.sdk.AppId";
    private static final String APPLICATION_NAME_PATH = "me.digi.sdk.AppName";
    private static final String DIGIME_PACKAGE = "me.digi.app3";
    private static final String CONSENT_ACCESS_CONTRACTS_PATH = "me.digi.sdk.Contracts";
    private static final String POSTBOX_CONTRACTS_PATH = "me.digi.sdk.PostboxContracts";

    private static CASession defaultSession;
    private final List<SDKListener> listeners = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<CASession, DigiMeAPIClient> networkClients;
    protected volatile DigiMeBaseAuthManager caAuthManager;
    private volatile DigiMePostboxAuthManager postboxAuthManager;

    private SessionManager<CASession> consentAccessSessionManager;

    public final Flow<CAContract> caFlow;
    public final Flow<CAContract> postboxFlow;

    private DigiMeClient() {
        this.networkClients = new ConcurrentHashMap<>();

        this.caFlow = new Flow<>(new FlowLookupInitializer<CAContract>() {
            @Override
            public CAContract create(String identifier) {
                return new CAContract(identifier, DigiMeClient.getApplicationId());
            }
        }, caContractIds);

        this.postboxFlow = new Flow<>(new FlowLookupInitializer<CAContract>() {
            @Override
            public CAContract create(String identifier) {
                return new CAContract(identifier, DigiMeClient.getApplicationId());
            }
        }, postboxContractIds);
    }

    private static Boolean clientInitialized = false;

    public static void viewReceiptInDigiMeApp() {
        checkClientInitialized();

        boolean isAppInstalled = appContext.getPackageManager().getLaunchIntentForPackage(DIGIME_PACKAGE) != null;

        if (!isAppInstalled) {
            throw new DigiMeException("Please install the app before going further!");
        } else if (caContractIds == null || caContractIds.length == 0)
            throw new DigiMeException("Allowed types for contract ID are only string-array or string. Check that you have set the correct meta-data type.");
        else if (caContractIds.length != 1) {
            throw new DigiMeException("Currently we only support 1 contract ID. Check that you have set the correct meta-data type.");
        } else {
            String url = "digime://receipt?contractid=" + caContractIds[0] + "&appid=" + applicationId;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            appContext.startActivity(intent);
        }
    }

    public static synchronized void init(
            final Context appContext) {
        if (clientInitialized) {
            return;
        }

        if (appContext == null) {
            throw new IllegalArgumentException("appContext can not be null.");
        }

        DigiMeClient.appContext = appContext.getApplicationContext();
        DigiMeClient.updatePropertiesFromMetadata(DigiMeClient.appContext);
        if ((applicationId == null) || (applicationId.length() == 0)) {
            throw new DigiMeException("Valid application ID must be set in manifest or by calling setApplicationId.");
        }

        clientInitialized = true;
        getInstance().onStart();
        defaultSession = new CASession("default", 0, "default", null, "default");
    }

    public static Executor getCoreExecutor() {
        synchronized (SYNC) {
            if (DigiMeClient.coreExecutor == null) {
                DigiMeClient.coreExecutor = AsyncTask.THREAD_POOL_EXECUTOR;
            }
        }
        return DigiMeClient.coreExecutor;
    }
    

    public static void checkClientInitialized() {
        if (!DigiMeClient.isClientInitialized()) {
            throw new DigiMeClientException("DigiMe Core Client has not been properly initialized. You need to call DigiMeClient.init().");
        }
    }

    public static synchronized boolean isClientInitialized() {
        return clientInitialized;
    }

    public static Context getApplicationContext() {
        checkClientInitialized();
        return appContext;
    }

    @SuppressWarnings("SameReturnValue")
    public static String getVersion() {
        return DigiMeSDKVersion.VERSION;
    }

    public static String getApplicationId() {
        checkClientInitialized();
        return applicationId;
    }

    public static String getApplicationName() {
        checkClientInitialized();
        return applicationName;
    }

    public static void setApplicationName(String applicationName) {
        DigiMeClient.applicationName = applicationName;
    }

    public static KeyLoaderProvider getDefaultKeyLoader() {
        checkClientInitialized();
        return loaderProvider;
    }

    private void onStart(){
        consentAccessSessionManager = new CASessionManager();
    }

    /*
     *  DigiMeClient instance methods
     */

    public static DigiMeClient getInstance() {
        checkClientInitialized();
        if (singleton == null) {
            synchronized (DigiMeClient.class) {
                singleton = new DigiMeClient();
            }
        }
        return singleton;
    }

    public SessionManager<CASession> getSessionManager() {
        checkClientInitialized();
        return consentAccessSessionManager;
    }

    public void addListener(@NonNull final SDKListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public boolean removeListener(@NonNull final SDKListener listener) {
        return this.listeners.remove(listener);
    }

    public DigiMeConsentAccessAuthManager createCAAuthManager() {
        caAuthManager = new DigiMeConsentAccessAuthManager();
        return (DigiMeConsentAccessAuthManager)caAuthManager;
    }

    public DigiMeGuestCAAuthManager createCAAuthManagerForGuest() {
        caAuthManager = new DigiMeGuestCAAuthManager();
        return (DigiMeGuestCAAuthManager)caAuthManager;
    }

    public DigiMeBaseAuthManager getCAAuthManager() {
        if (caAuthManager == null) {
            synchronized (DigiMeClient.class) {
                if (caAuthManager == null) {
                    return createCAAuthManager();
                }
            }
        }
        return caAuthManager;
    }

    public DigiMePostboxAuthManager getPostboxAuthManager() {
        if (postboxAuthManager == null) {
            synchronized (DigiMeClient.class) {
                if (postboxAuthManager == null) {
                    postboxAuthManager = new DigiMePostboxAuthManager();
                }
            }
        }
        return postboxAuthManager;
    }

    /**
     *  Public methods
     */

    public DigiMeConsentAccessAuthManager authorize(@NonNull Activity activity, @Nullable SDKCallback<CASession> callback) {
        checkClientInitialized();
        DigiMeConsentAccessAuthManager authManager = createCAAuthManager();
        SDKCallback<CASession> forwarder = new AutoSessionForwardCallback<>(authManager, activity, callback);
        authManager.resolveAuthorizationPath(activity, forwarder, false);
        return authManager;
    }

    public DigiMeConsentAccessAuthManager authorize(@NonNull Activity activity, TimeRange timeRange, @Nullable SDKCallback<CASession> callback) {
        return authorize(activity, Collections.singletonList(timeRange), callback);
    }

    public DigiMeConsentAccessAuthManager authorize(@NonNull Activity activity, List<TimeRange> timeRanges, @Nullable SDKCallback<CASession> callback) {
        checkClientInitialized();
        DigiMeConsentAccessAuthManager authManager = createCAAuthManager();
        SDKCallback<CASession> forwarder = new AutoSessionForwardCallback<>(authManager, activity, callback);
        authManager.setScope(new DataRequest((timeRanges)));
        authManager.resolveAuthorizationPath(activity, forwarder, false);
        return authManager;
    }

    /**
     *  Guest Consent flow (Quark)
     */
    public DigiMeGuestCAAuthManager authorizeGuest(@NonNull Activity activity, @Nullable SDKCallback<CASession> callback) {
        checkClientInitialized();
        DigiMeGuestCAAuthManager authManager = createCAAuthManagerForGuest();
        SDKCallback<CASession> forwarder = new AutoSessionForwardCallback<>(authManager, activity, callback);
        authManager.resolveAuthorizationPath(activity, forwarder, false);
        return authManager;
    }

    public DigiMeGuestCAAuthManager authorizeGuest(@NonNull Activity activity, TimeRange timeRange, @Nullable SDKCallback<CASession> callback) {
        return authorizeGuest(activity, Collections.singletonList(timeRange), callback);
    }

    public DigiMeGuestCAAuthManager authorizeGuest(@NonNull Activity activity, List<TimeRange> timeRanges, @Nullable SDKCallback<CASession> callback) {
        checkClientInitialized();
        DigiMeGuestCAAuthManager authManager = createCAAuthManagerForGuest();
        SDKCallback<CASession> forwarder = new AutoSessionForwardCallback<>(authManager, activity, callback);
        authManager.setScope(new DataRequest(timeRanges));
        authManager.resolveAuthorizationPath(activity, forwarder, false);
        return authManager;
    }

    public DigiMePostboxAuthManager createPostbox(@NonNull Activity activity, @Nullable SDKCallback<SessionResult> callback) {
        checkClientInitialized();
        SDKCallback<SessionResult> forwarder = new AutoSessionForwardCallback<>(getPostboxAuthManager(), activity, callback);
        getPostboxAuthManager().resolveAuthorizationPath(activity, forwarder, false);
        return getPostboxAuthManager();
    }

    public <T extends SessionResult> void createSession(@Nullable SDKCallback<T>callback, @Nullable DataRequest scope) throws DigiMeException {
        if (!caFlow.isInitialized()) {
            throw new DigiMeException("No CA contracts registered! You must have forgotten to add contract Id to the meta-data path \"%s\" or pass the CAContract object to createSession.", CONSENT_ACCESS_CONTRACTS_PATH);
        }
        if (!caFlow.next()) { caFlow.rewind().next(); }
        createSession(caFlow.currentId, scope, callback);
    }

    public <T extends SessionResult> void createSession(@NonNull String contractId, @Nullable DataRequest scope, @Nullable SDKCallback<T>callback) {
        createSession(caFlow, contractId, scope, callback);
    }

    private <T extends SessionResult> void createSession(Flow<CAContract> flow, @NonNull String contractId, @Nullable DataRequest scope, @Nullable SDKCallback<T>callback) {
        boolean useFlow = false;
        CAContract contract;
        if (flow.isInitialized()) {
            useFlow = flow.stepTo(contractId);
        }
        if (useFlow) {
            contract = flow.get();
        } else {
            if (!Util.validateContractId(contractId) && DigiMeClient.debugEnabled) {
                throw new DigiMeException("Provided contractId has invalid format.");
            }
            contract = new CAContract(contractId, DigiMeClient.getApplicationId());
        }
        contract.setScope(scope);
        startSessionWithContract(contract, callback);
    }

    public <T extends SessionResult> void createPostboxSession(@Nullable SDKCallback<T>callback) throws DigiMeException {
        if (!postboxFlow.isInitialized()) {
            throw new DigiMeException("No Postbox contracts registered! You must have forgotten to add contract Id to the meta-data path \"%s\" or pass the CAContract object to createSession.", POSTBOX_CONTRACTS_PATH);
        }
        if (!postboxFlow.next()) { postboxFlow.rewind().next(); }
        createSession(postboxFlow.currentId, null, callback);
    }

    private void createPostboxSession(@NonNull String contractId, @Nullable SDKCallback<SessionResult>callback) {
        createSession(postboxFlow, contractId, null, callback);
    }

    public <T extends SessionResult> void startSessionWithContract(CAContract contract, @Nullable SDKCallback<T> callback) {
        checkClientInitialized();
        DigiMeAPIClient client = getDefaultApi();
        SessionForwardCallback dispatchCallback;
        if (callback instanceof AutoSessionForwardCallback) {
            dispatchCallback = (AutoSessionForwardCallback) callback;
        } else {
            dispatchCallback = new SessionForwardCallback<>(callback);
        }
        if (contract == null) {
            dispatchCallback.failed(new SDKValidationException("Contract is null. Session can not be initialized!"));
        }
        client.sessionService().getSessionToken(contract).enqueue(dispatchCallback);
    }

    public void getFileList(@Nullable SDKCallback<CAFiles> callback) {
        getFileListWithSession(getSessionManager().getCurrentSession(), callback);
    }

    public void getFileListWithSession(CASession session, @Nullable SDKCallback<CAFiles> callback) {
        checkClientInitialized();
        ContentForwardCallback<CAFiles> proxy = new ContentForwardCallback<>(callback, null, CAFiles.class);
        if (!validateSession(session, proxy)) return;
        //noinspection ConstantConditions
        getApi().consentAccessService().list(session.sessionKey)
                .enqueue(proxy);
    }

    public void getAccounts(@Nullable SDKCallback<CAAccounts> callback) {
        getAccountsWithSession(getSessionManager().getCurrentSession(), callback);
    }

    public void getAccountsWithSession(CASession session, @Nullable SDKCallback<CAAccounts> callback) {
        checkClientInitialized();
        ContentForwardCallback<CAAccounts> proxy = new ContentForwardCallback<>(callback, null, CAAccounts.class);
        if (!validateSession(session, proxy)) return;
        //noinspection ConstantConditions
        getApi().consentAccessService().accounts(session.sessionKey)
                .enqueue(proxy);
    }

    /**
     * @deprecated Retrieving data as an object is no longer supported. Please use {@link #getFile(String, SDKCallback)} instead.
     */
    @Deprecated
    public void getFileContent(String fileId, @Nullable SDKCallback<CAFileResponse> callback) {
        getFileContentWithSession(fileId, getSessionManager().getCurrentSession(), callback);
    }

    /**
     * @deprecated Retrieving data as an object is no longer supported. Please use {@link #getFileWithSession(String, CASession, SDKCallback)} instead.
     */
    @Deprecated
    public void getFileContentWithSession(String fileId, CASession session, @Nullable SDKCallback<CAFileResponse> callback) {
        checkClientInitialized();
        ContentForwardCallback<CAFileResponse> proxy = new ContentForwardCallback<>(callback, fileId, CAFileResponse.class);
        if (!validateSession(session, proxy)) return;
        if (fileId == null) {
            throw new IllegalArgumentException("File ID can not be null.");
        }
        //noinspection ConstantConditions
        getApi().consentAccessService().data(session.sessionKey, fileId)
                .enqueue(proxy);
    }

    /**
     * @deprecated This method has been renamed to {@link #getFile(String, SDKCallback)}.
     */
    @Deprecated
    public void getFileJSON(String fileId, @Nullable SDKCallback<JsonElement> callback) {
        getFile(fileId, callback);
    }

    /**
     * @deprecated This method has been renamed to {@link #getFileWithSession(String, CASession, SDKCallback)}.
     */
    @Deprecated
    public void getFileJSONWithSession(String fileId, CASession session, @Nullable SDKCallback<JsonElement> callback) {
        getFileWithSession(fileId, session, callback);
    }

    public void getFile(String fileId, @Nullable SDKCallback<JsonElement> callback) {
        getFileWithSession(fileId, getSessionManager().getCurrentSession(), callback);
    }

    public void getFileWithSession(String fileId, CASession session, @Nullable SDKCallback<JsonElement> callback) {
        checkClientInitialized();
        ContentForwardCallback<JsonElement> proxy = new ContentForwardCallback<>(callback, fileId, JsonElement.class);
        if (!validateSession(session, proxy)) return;
        if (fileId == null) {
            throw new IllegalArgumentException("File ID can not be null.");
        }
        //noinspection ConstantConditions
        getApi().consentAccessService().dataRaw(session.sessionKey, fileId)
                .enqueue(proxy);
    }

    public DigiMeAPIClient getDefaultApi() {
        return getApi(defaultSession);
    }

    public DigiMeAPIClient getApi() {
        checkClientInitialized();
        final CASession session = consentAccessSessionManager.getCurrentSession();
        if (session == null) {
            return null;
        }
        return getApi(session);
    }

    public DigiMeAPIClient getApi(CASession session) {
        checkClientInitialized();
        if (!networkClients.containsKey(session)) {
            networkClients.putIfAbsent(session, new DigiMeAPIClient());
        }
        return networkClients.get(session);
    }

    public DigiMeAPIClient addCustomClient(OkHttpClient client) {
        checkClientInitialized();
        final CASession session = consentAccessSessionManager.getCurrentSession();
        if (session == null) {
            return null;
        }
        return addCustomClient(session, client, null);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public DigiMeAPIClient addCustomClient(CASession session, OkHttpClient client, ApiConfig apiConfig) {
        checkClientInitialized();
        DigiMeAPIClient apiClient;
        ApiConfig realConfig = apiConfig == null ? ApiConfig.get() : apiConfig;
        if (client == null) {
            apiClient = new DigiMeAPIClient();
        } else {
            apiClient = new DigiMeAPIClient(client, realConfig);
        }
        return networkClients.put(session, apiClient);
    }

    public DigiMeBaseAuthManager authorizeInitializedSession(@NonNull Activity activity, @Nullable SDKCallback<CASession> callback) {
        checkClientInitialized();
        this.authorizeInitializedSessionWithManager(this.getCAAuthManager(), activity, callback);
        return this.getCAAuthManager();
    }

    /**
     *  Private helpers
     */

    private <T extends SessionResult> void authorizeInitializedSessionWithManager(DigiMeBaseAuthManager authManager, @NonNull Activity activity, @Nullable SDKCallback<T> callback) {
        if (authManager == null) {
            throw new IllegalArgumentException("Authorization Manager can not be null.");
        }
        SDKCallback<SessionResult> forwarder = (callback != null && callback instanceof AuthorizationForwardCallback) ? (SDKCallback<SessionResult>) callback : new AuthorizationForwardCallback(callback);

        if (!authManager.canHandleAuthImmediately(activity)) {
            forwarder = new AutoSessionForwardCallback(authManager, activity, callback);
        }

        authManager.resolveAuthorizationPath(activity, forwarder, true);
    }

    private static void updatePropertiesFromMetadata(Context context) {
        if (context == null) {
            return;
        }
        ApplicationInfo ai;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        if (ai == null || ai.metaData == null) {
            return;
        }
        if (applicationId == null) {
            Object appId = ai.metaData.get(APPLICATION_ID_PATH);
            if (appId instanceof String) {
                applicationId = (String) appId;
            } else if (appId instanceof Integer) {
                throw new DigiMeException(
                        "App ID must be placed in the strings manifest file");
            }
        }

        if (applicationName == null) {
            applicationName = ai.metaData.getString(APPLICATION_NAME_PATH);
        }

        if (caContractIds == null) {
            caContractIds = extractContracts(context, ai, CONSENT_ACCESS_CONTRACTS_PATH);
        }

        if (postboxContractIds == null) {
            postboxContractIds = extractContracts(context, ai, POSTBOX_CONTRACTS_PATH);
        }

        if (caContractIds == null && postboxContractIds == null)
            throw new DigiMeException(
                "Allowed types for contract ID are only string-array or string. Check that you have set the correct meta-data type.");

        if (loaderProvider == null) {
            loaderProvider = new KeyLoaderProvider(ai.metaData, context);
        }
    }

    @Nullable
    private static String[] extractContracts(Context context, ApplicationInfo ai, String path) {
        Object contract = ai.metaData.get(path);
        if (contract instanceof String) {
            String cont = (String) contract;
            return new String[]{cont};
        } else if (contract instanceof Integer) {
            String type = context.getResources().getResourceTypeName((int) contract);
            if (type.equalsIgnoreCase("array")) {
                return context.getResources().getStringArray((int)contract);
            } else if (type.equalsIgnoreCase("string")) {
                String cnt = context.getResources().getString((int)contract);
                return new String[]{cnt};
            }
        }
        return null;
    }

    private boolean validateSession(SDKCallback callback) {
        boolean valid = false;
        if (getSessionManager().getCurrentSession() != null && getSessionManager().getCurrentSession().isValid()) {
            valid = true;
        }
        if (!valid && callback != null) {
            callback.failed(new SDKValidationException("Current session is null or invalid", SDKValidationException.SESSION_VALIDATION_ERROR));
        }
        return valid;
    }

    private boolean validateSession(CASession session, SDKCallback callback) throws IllegalArgumentException {
        boolean valid = false;
        if (session == null) {
            if (callback == null) {
                throw new IllegalArgumentException("Session can not be null.");
            } else {
                callback.failed(new SDKValidationException("Current session is null", SDKValidationException.SESSION_VALIDATION_ERROR));
            }
        } else if (session.isValid()) {
            valid = true;
        }
        if (!valid && callback != null) {
            callback.failed(new SDKValidationException("Current session is invalid", SDKValidationException.SESSION_VALIDATION_ERROR));
        }
        return valid;
    }

    /**
     *  Iterator for pre-registered CAContract flow
     *
     */

    abstract class FlowLookupInitializer<T> {

        public abstract T create(String identifier);
    }

    public static final class Flow<T> {
        static final int START_MARKER = Integer.MAX_VALUE;

        private String currentId;
        private int currentStep;
        private final ArrayList<String> identifiers;
        private final ConcurrentHashMap<String, T> lookup;

        private Flow(String[] ids) {
            this.lookup = new ConcurrentHashMap<>();
            if (ids == null || ids.length == 0) {
                this.identifiers = new ArrayList<>();
            } else {
                this.identifiers = new ArrayList<>(Arrays.asList(ids));
            }
            tryInit();
        }

        private Flow(FlowLookupInitializer<T> initializer, String[] ids) {
            this(ids);
            if (this.isInitialized()) {
                for (String id :
                        identifiers) {
                    this.lookup.putIfAbsent(id, initializer.create(id));
                }
            }
        }

        private void tryInit() {
            if (identifiers == null) {
                currentStep = -1;
                currentId = null;
            } else if (identifiers.size() == 0) {
                currentStep = -1;
                currentId = null;
            } else {
                currentStep = START_MARKER;
                currentId = null;
            }
        }

        public int getCurrentStep() {
            return currentStep;
        }

        public String getCurrentId() {
            return currentId;
        }

        public boolean isInitialized() {
            return !(currentStep < 0 || (currentStep != START_MARKER && currentId == null));
        }

        public boolean next() {
            if (identifiers == null) { return false; }
            if (currentStep == START_MARKER) { currentStep = -1; }
            if (currentStep + 1 >= identifiers.size()) { return false; }
            currentStep++;
            currentId = identifiers.get(currentStep);

            return true;
        }

        public T get() {
            if (!isInitialized()) { return null; }
            return lookup.get(currentId);
        }

        public boolean stepTo(String identifier) {
            if (identifier == null) { return false; }
            if (identifier.equals(currentId)) { return true; }
            if (lookup.containsKey(identifier)) {
                int index = identifiers.indexOf(identifier);
                if (index >= 0) {
                    currentId = identifier;
                    currentStep = index;
                    return true;
                }
            }
            return false;
        }

        public Flow rewind() {
            tryInit();
            return this;
        }
    }

    /**
     *  Callback wrappers
     */


    class SessionForwardCallback <T extends SessionResult> extends SDKCallback<T> {
        final SDKCallback<T> callback;

        SessionForwardCallback(SDKCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void succeeded(SDKResponse<T> result) {
            final CASession session = result.body.session();
            if (session == null) {
                callback.failed(new SDKException("Session create returned an empty session!"));
                return;
            }
            CASessionManager sm = (CASessionManager)consentAccessSessionManager;
            sm.setCurrentSession(session);
            getInstance().getApi(session);
            if (callback != null && !(this instanceof AutoSessionForwardCallback)) {
                callback.succeeded(new SDKResponse<>(result.body, result.response));
            }
            for (SDKListener listener : listeners) {
                listener.sessionCreated(session);
            }
        }

        @Override
        public void failed(SDKException exception) {
            if (callback != null) {
                callback.failed(exception);
            }
            for (SDKListener listener : listeners) {
                listener.sessionCreateFailed(exception);
            }
        }
    }

    private class AutoSessionForwardCallback<T extends SessionResult> extends SessionForwardCallback<T> {
        private final WeakReference<Activity> callActivity;
        private final WeakReference<DigiMeBaseAuthManager> authManager;

        AutoSessionForwardCallback(DigiMeBaseAuthManager authManager, Activity activity, SDKCallback<T> callback) {
            super(callback);
            this.callActivity = new WeakReference<>(activity);
            this.authManager = new WeakReference<>(authManager);
        }

        @Override
        public void succeeded(SDKResponse<T> result) {
            super.succeeded(result);
            Activity activity = callActivity.get();
            DigiMeBaseAuthManager am = authManager.get();
            if (activity != null && am != null) {
                authorizeInitializedSessionWithManager(am, activity, callback);
            }
        }
        @Override
        public void failed(SDKException exception) {
            super.failed(exception);
        }
    }

    private class AuthorizationForwardCallback<T extends SessionResult> extends SDKCallback<T> {
        private final SDKCallback<T> callback;

        AuthorizationForwardCallback(SDKCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void succeeded(SDKResponse<T> result) {
            if (callback != null) {
                callback.succeeded(result);
            }
            for (SDKListener listener : listeners) {
                listener.authorizeSucceeded(result.body.session());
                if (listener instanceof SDKPostboxListener && result.body instanceof CreatePostboxSession)
                    ((SDKPostboxListener)listener).postboxCreated(((CreatePostboxSession) result.body).postboxId);
            }
        }

        @Override
        public void failed(SDKException exception) {
            if (exception instanceof AuthorizationException) {
                determineReason((AuthorizationException) exception);
            } else if (callback != null) {
                callback.failed(exception);
            }
        }

        private void determineReason(AuthorizationException exception) {
            AuthorizationException.Reason reason = exception.getThrowReason();

            if (callback != null && reason != AuthorizationException.Reason.WRONG_CODE) {
                callback.failed(exception);
            }
            for (SDKListener listener : listeners) {
                switch (reason) {
                    case ACCESS_DENIED:
                        listener.authorizeDenied(exception);
                        break;
                    case WRONG_CODE:
                    case IN_PROGRESS:
                        listener.authorizeFailedWithWrongRequestCode();
                        break;
                }
            }
        }
    }

    private class ContentForwardCallback<T> extends SDKCallback<T> {
        final SDKCallback<T> callback;
        final String reserved;
        private final Class<T> type;

        ContentForwardCallback(SDKCallback<T> callback, Class<T> type) {
            this(callback, null, type);
        }

        ContentForwardCallback(SDKCallback<T> callback, String additionalData, Class<T> type) {
            this.callback = callback;
            this.reserved = additionalData;
            this.type = type;
        }

        @Override
        public void succeeded(SDKResponse<T> result) {
            if (callback != null) {
                callback.succeeded(result);
            }
            T returnedObject = result.body;

            for (SDKListener listener : listeners) {
                if (returnedObject instanceof CAFiles) {
                    listener.clientRetrievedFileList((CAFiles) returnedObject);
                } else if (returnedObject instanceof CAFileResponse) {
                    listener.contentRetrievedForFile(reserved, (CAFileResponse) returnedObject);
                } else if (returnedObject instanceof JsonElement) {
                    listener.jsonRetrievedForFile(reserved, (JsonElement) returnedObject);
                } else if (returnedObject instanceof CAAccounts) {
                    listener.accountsRetrieved((CAAccounts) returnedObject);
                }
            }
        }

        @Override
        public void failed(SDKException exception) {
            if (callback != null) {
                callback.failed(exception);
            }
            for (SDKListener listener : listeners) {
                if (type.equals(CAFiles.class)) {
                    listener.clientFailedOnFileList(exception);
                } else if (type.equals(CAFileResponse.class)) {
                    listener.contentRetrieveFailed(reserved, exception);
                } else if (type.equals(JsonElement.class)) {
                    listener.contentRetrieveFailed(reserved, exception);
                } else if (type.equals(CAAccounts.class)) {
                    listener.accountsRetrieveFailed(exception);
                }
            }
        }
    }
}
