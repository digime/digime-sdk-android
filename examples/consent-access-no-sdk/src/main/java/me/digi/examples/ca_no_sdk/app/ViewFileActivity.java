/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import me.digi.examples.ca_no_sdk.R;
import me.digi.examples.ca_no_sdk.service.GetUserDataTask;
import me.digi.examples.ca_no_sdk.service.PermissionService;
import me.digi.examples.ca_no_sdk.service.models.DataGetResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Locale;

import retrofit2.Response;

public class ViewFileActivity extends LoadingActivity implements GetUserDataTask.Listener {
    public static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
    public static final String KEY_FILE_NAME = "KEY_FILE_NAME";

    public static Intent getViewFileActivityIntent(Activity activity, String sessionToken, String fileName) {
        Intent intent = new Intent(activity, ViewFileActivity.class);
        intent.putExtra(KEY_SESSION_TOKEN, sessionToken);
        intent.putExtra(KEY_FILE_NAME, fileName);
        return intent;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private TextView fileName;
    private TextView fileJson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_view_file);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileName = (TextView) findViewById(R.id.file_name);
        fileJson = (TextView) findViewById(R.id.file_json);

        Intent intent = getIntent();
        if (intent != null) {
            String sessionToken = intent.getStringExtra(KEY_SESSION_TOKEN);
            String fileNameString = intent.getStringExtra(KEY_FILE_NAME);
            if (!TextUtils.isEmpty(sessionToken) && !TextUtils.isEmpty(fileNameString)) {
                fileName.setText(fileNameString);
                sendGetDataRequest(sessionToken, fileNameString);
            }
        }
    }

    private void sendGetDataRequest(String sessionToken, String fileName) {
        // Logging.
        addLog("Calling [query/sessionKey/fileName]");

        // Display loading screen.
        contentLoading(getResources().getString(R.string.loading_getting_file_data));

        // Get user data in background task for specific file.
        PermissionService permissionService = ((Application) getApplication()).getPermissionService();
        new GetUserDataTask().execute(new GetUserDataTask.GetUserDataTaskParams(getApplicationContext(), permissionService, sessionToken, fileName, this));
    }

    @Override
    public void userDataTaskComplete(Response<DataGetResponse> response) {
        contentFinishedLoading();
        if (dealWithResponse("[query/sessionKey/fileName]", response, this)) {
            addLog(String.format("[query/sessionKey/fileName]" + " successful (%s).", String.valueOf(response.code())));
            showFileJson(response.body());
        }
    }

    private void showFileJson(DataGetResponse dataGetResponse) {
        String toDisplay = "";
        int fileSize = dataGetResponse.fileContent.size();

        if (fileSize == 0) {
            toDisplay = "File has no objects to display.";
        } else if (fileSize > 1) {
            toDisplay = String.format(Locale.getDefault(), "File has %d objects, showing first.\n\n", fileSize);
            toDisplay += new Gson().toJson(dataGetResponse.fileContent.get(0), JsonElement.class);
        }

        if (fileSize == 1) {
            toDisplay = String.format(Locale.getDefault(), "File has %d objects\n", fileSize);
            toDisplay += new Gson().toJson(dataGetResponse.fileContent.get(0), JsonElement.class);
        }

        fileJson.setText(toDisplay);
    }

    @Override
    public void userDataTaskFailed(Exception e) {
        throw new RuntimeException("[query/sessionKey/fileName] task failed.", e);
    }

    @Override
    public void onBackPressed() {
        Intent intent = activityOutgoing(new Intent());
        setResult(RESULT_OK, intent);
        finish();
    }
}
