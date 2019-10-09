/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.examples.postbox;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import me.digi.sdk.core.CreatePostboxSession;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKResponse;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.session.SessionResult;

public class MainActivity extends AppCompatActivity {
    private View progressBar;
    private View dimScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dimScreen = findViewById(R.id.dim_screen);
        progressBar = findViewById(R.id.progress_bar);

        View createPostboxButton = findViewById(R.id.create_postbox_button);
        createPostboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                createPostbox();
            }
        });

        setClickAction(R.id.postbox_id, getString(R.string.postbox_id_label));
        setClickAction(R.id.postbox_public_key, getString(R.string.postbox_public_key_label));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DigiMeClient.getInstance().getPostboxAuthManager().onActivityResult(requestCode, resultCode, data);
    }

    private void createPostbox() {
        DigiMeClient.getInstance().createPostbox(this, new SDKCallback<SessionResult>() {
            @Override
            public void succeeded(SDKResponse<SessionResult> result) {
                if (result.body instanceof CreatePostboxSession) {
                    CreatePostboxSession postboxSession = (CreatePostboxSession) result.body;

                    String postboxId = postboxSession.postboxId;
                    String postboxPublicKey = postboxSession.postboxPublicKey;

                    setText(R.id.postbox_id, postboxId);
                    setText(R.id.postbox_public_key, postboxPublicKey);
                } else {
                    logException("Success! But body is not recognised!", new Exception("Body isn't CreatePostboxSession instance!"));
                }
                hideProgressBar();
            }

            @Override
            public void failed(SDKException exception) {
                logException("Postbox creation failed!", exception);
                hideProgressBar();
            }
        });
    }

    private void setText(@IdRes int textViewRes, String text) {
        TextView textView = findViewById(textViewRes);
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void setClickAction(@IdRes int textViewRes, final String label) {
        TextView textView = findViewById(textViewRes);
        textView.setOnClickListener(new CopyToClipboardAction(label));
    }

    private void logException(String text, Exception exception) {
        Log.e("postbox-demo", text, exception);
        showToast(text);
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void showProgressBar() {
        ActivityUtils.showProgressAndDimScreen(progressBar, dimScreen);
    }

    private void hideProgressBar() {
        ActivityUtils.hideProgressAndRemoveDimScreen(progressBar, dimScreen, null);
    }

    private class CopyToClipboardAction implements View.OnClickListener {
        private String label;

        private CopyToClipboardAction(String label) {
            this.label = label;
        }

        @Override
        public void onClick(View view) {
            try {
                TextView textView = (TextView) view;
                String text = textView.getText().toString();
                if (!text.isEmpty()) {
                    copyToClipboard(label, text);
                    showToast("Text has been copied to clipboard!");
                }
            } catch (Exception exception) {
                logException("Failed to copy into clipboard!", exception);
            }
        }

        private void copyToClipboard(String label, String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            Objects.requireNonNull(clipboard);

            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
    }
}