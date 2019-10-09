/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import me.digi.examples.ca_no_sdk.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String KEY_LOG = "KEY_LOG";
    protected static final String KEY_LOG_SHOWN = "KEY_LOG_SHOWN";

    protected ScrollView logScroll;
    protected TextView consoleLog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logScroll = (ScrollView) findViewById(R.id.log_scroll);
        consoleLog = (TextView) findViewById(R.id.console_log);
        activityReturning(getIntent());
    }

    /**
     * Outputs toast if response has failed
     *
     * @param apiName
     * @param response
     * @param context
     * @return true is response was successful
     */
    protected boolean dealWithResponse(String apiName, Response response, Context context) {
        int code = response.code();
        if (response.errorBody() == null) {
            return true;
        }

        String errorBody;
        try {
            errorBody = response.errorBody().string();
        } catch (IOException e) {
            e.printStackTrace();
            unknownError(apiName, code, context);
            return false;
        }

        if (TextUtils.isEmpty(errorBody)) {
            return true;
        } else {
            try {
                JSONObject jsonObject = new JSONObject(errorBody);
                String errorMessage = jsonObject.getString("error");

                if (!TextUtils.isEmpty(errorMessage)) {
                    addLog(String.format(apiName + " unsuccessful - %s (%s).", errorMessage, String.valueOf(code)));
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    unknownError(apiName, code, context);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                unknownError(apiName, code, context);
            }
        }
        return false;
    }


    private void unknownError(String apiName, int code, Context context) {
        addLog(String.format(apiName + " unsuccessful Unknown error (%s).", String.valueOf(code)));
        Toast.makeText(context, String.format(Locale.getDefault(), "Unknown error (%d)", code), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.console_only_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = activityOutgoing(new Intent());
                setResult(RESULT_OK, intent);
                finish();
            case R.id.show_console:
                logScroll.setVisibility(View.VISIBLE);
                return true;
            case R.id.hide_console:
                logScroll.setVisibility(View.GONE);
                return true;
            case R.id.clear_console:
                consoleLog.setText("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void activityReturning(Intent intent) {
        if (intent != null) {
            String log = intent.getStringExtra(KEY_LOG);
            consoleLog.setText(log);

            if (intent.getBooleanExtra(KEY_LOG_SHOWN, false)) {
                logScroll.setVisibility(View.VISIBLE);
            } else {
                logScroll.setVisibility(View.GONE);
            }
        }
    }

    protected Intent activityOutgoing(Intent intent) {
        if (intent != null) {
            intent.putExtra(KEY_LOG, consoleLog.getText().toString());
            intent.putExtra(KEY_LOG_SHOWN, logScroll.getVisibility() == View.VISIBLE);
            return intent;
        } else {
            throw new RuntimeException("Activity outgoing intent must not be null");
        }
    }

    protected void addLog(String log) {
        consoleLog.setText(consoleLog.getText().toString() +
            android.text.format.DateFormat.format("hh:mm:ss", new Date()) + ": " + log
            + "\n");
        logScroll.post(new Runnable() {
            @Override
            public void run() {
                logScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_LOG_SHOWN, logScroll.getVisibility() == View.VISIBLE);
        outState.putString(KEY_LOG, consoleLog.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        logScroll.setVisibility(savedInstanceState.getBoolean(KEY_LOG_SHOWN, false) ? View.VISIBLE : View.GONE);
        consoleLog.setText(savedInstanceState.getString(KEY_LOG));
        super.onRestoreInstanceState(savedInstanceState);
    }
}
