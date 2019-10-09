/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core.internal.quark;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import me.digi.sdk.core.BuildConfig;
import me.digi.sdk.core.R;
import me.digi.sdk.core.errorhandling.SDKException;

public class QuarkBrowserActivity extends Activity {

    public static final String EXTRA_SESSION_EXCHANGE_TOKEN = "EXTRA_SESSION_EXCHANGE_TOKEN";
    public static final String EXTRA_RESULT_ERROR = "EXTRA_RESULT_ERROR";
    private static final String RESULT_DATA_READY = "DATA_READY";
    private static final String RESULT_CANCELLED = "CANCELLED";
    private String callbackUrl;
    private ProgressBar spinner;

    public static void startForResult(Activity activity, String sessionExchangeToken, int requestCode) {
        final Intent intent = new Intent(activity, QuarkBrowserActivity.class);
        intent.putExtra(EXTRA_SESSION_EXCHANGE_TOKEN, sessionExchangeToken);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quark_main_view);
        callbackUrl = getString(R.string.quark_callback_scheme) + "://quark-return/";
        spinner = findViewById(R.id.quark_main_spinner);

        Intent incomingIntent = getIntent();
        String sessionExchangeToken = null;

        if (incomingIntent != null) {
            Uri uri = incomingIntent.getData();
            if (handleQuarkCallback(uri)) {
                return;
            } else {
                sessionExchangeToken = incomingIntent.getStringExtra(EXTRA_SESSION_EXCHANGE_TOKEN);
                if (TextUtils.isEmpty(sessionExchangeToken)) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
            }
        }
        spinner.setVisibility(View.VISIBLE);
        onBoardWithQuark(sessionExchangeToken);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleQuarkCallback(intent.getData());
    }

    private boolean handleQuarkCallback(Uri uri) {
        if (uri != null && uri.getScheme() != null) {
            if (uri.getScheme().equals(getString(R.string.quark_callback_scheme))) {
                spinner.setVisibility(View.GONE);
                String result = uri.getQueryParameter("result");
                if (result == null || result.equals(RESULT_CANCELLED))
                    onAuthCancelled();
                else if (result.equals(RESULT_DATA_READY)){
                    setResult(RESULT_OK);
                    finish();
                }
                return true;
            }
        }
        return false;
    }

    private void onBoardWithQuark(String sessionExchangeToken) {
        Uri uri = Uri.parse("https://" + BuildConfig.BASE_HOST + "/apps/quark/direct-onboarding")
            .buildUpon()
            .appendQueryParameter("sessionExchangeToken", sessionExchangeToken)
            .appendQueryParameter("callbackUrl", callbackUrl)
            .build();
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @Override
    public void onBackPressed() {
        onAuthCancelled();
    }

    private void onAuthCancelled() {
        final Intent data = new Intent();
        data.putExtra(EXTRA_RESULT_ERROR, new SDKException("Authorization cancelled!"));
        setResult(RESULT_CANCELED, data);
        finish();
    }

}
