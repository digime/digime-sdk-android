/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import me.digi.examples.ca_no_sdk.BuildConfig;
import me.digi.examples.ca_no_sdk.R;
import me.digi.examples.ca_no_sdk.service.GetSessionTokenTask;
import me.digi.examples.ca_no_sdk.service.PermissionService;
import me.digi.examples.ca_no_sdk.service.models.SessionKeyCreateResponse;
import me.digi.examples.ca_no_sdk.service.models.SessionTokenBody;

import java.util.List;

import retrofit2.Response;

public class MainActivity extends LoadingActivity implements GetSessionTokenTask.Listener {
    private static final String APP_ID = BuildConfig.APP_ID; // Replace with real Application ID in build.gradle.
    private static final String CONTRACT_ID = BuildConfig.CONTRACT_ID; // Replace with real contract ID in build.gradle.
    private static final String PERMISSION_ACCESS_INTENT_ACTION = "android.intent.action.DIGI_PERMISSION_REQUEST";
    private static final String PERMISSION_ACCESS_INTENT_TYPE = "text/plain";
    private static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
    private static final String KEY_APP_ID = "KEY_APP_ID";
    private static final int REQUEST_CODE = 100;

    private String sessionToken;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activityReturning(data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                addLog("Permission granted.");
                Intent intent = PermissionGrantedActivity.getPermissionGrantedActivityIntent(MainActivity.this, sessionToken);
                activityOutgoing(intent);
                startActivityForResult(intent, 0);
            } else {
                addLog("Permission declined.");
                Toast.makeText(this, "Request declined.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            getSessionTokenFromServer();
            }
        });
    }

    private void getSessionTokenFromServer() {
        // Logging.
        addLog("Calling [session/create].");

        // Display loading screen.
        contentLoading(getResources().getString(R.string.loading_getting_session_key));

        // Get session token in background task.
        PermissionService permissionService = ((Application) getApplication()).getPermissionService();
        new GetSessionTokenTask().execute(new GetSessionTokenTask.GetSessionTokenTaskParams(permissionService, new SessionTokenBody(APP_ID, CONTRACT_ID), MainActivity.this));
    }

    @Override
    public void sessionTokenTaskComplete(Response<SessionKeyCreateResponse> response) {
        // Hide loading screen.
        contentFinishedLoading();

        if (dealWithResponse("[session/create]", response, this)) {
            addLog(String.format("[session/create] successful (%s).", String.valueOf(response.code())));

            // Send request to digi.me Application.
            sendRequest(response.body().sessionKey);
        }
    }

    @Override
    public void sessionTokenTaskFailed(Exception e) {
        contentFinishedLoading();
        throw new RuntimeException("RequestSessionToken task failed.", e);
    }

    private void sendRequest(String sessionToken) {
        this.sessionToken = sessionToken;
        Intent sendIntent = new Intent();
        sendIntent.setAction(PERMISSION_ACCESS_INTENT_ACTION);
        sendIntent.putExtra(KEY_SESSION_TOKEN, sessionToken);
        sendIntent.putExtra(KEY_APP_ID, APP_ID);
        sendIntent.setType(PERMISSION_ACCESS_INTENT_TYPE);

        if (verifyIntentCanBeHandled(sendIntent)) {
            addLog("Sending request to digi.me application.");
            activityOutgoing(sendIntent);
            startActivityForResult(sendIntent, REQUEST_CODE);
        } else {
            startInstallDigiMeFlow();
        }
    }

    private boolean verifyIntentCanBeHandled(@NonNull Intent intent) {
        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    private void startInstallDigiMeFlow() {
        // Launch Intent to go to digi.me application download page (NOT DONE).
        addLog("digi.me application not found.");
        Toast.makeText(this, "digi.me not found.", Toast.LENGTH_LONG).show();
    }
}
