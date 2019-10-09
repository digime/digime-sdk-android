/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Locale;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKResponse;
import me.digi.sdk.core.entities.CAAccounts;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.session.CASession;

public class CallbackActivity extends LogActivity {
    public static final String FOR_GUEST_EXTRA = "for_guest_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKCallback<CASession> cb = new SDKCallback<CASession>() {
            @Override
            public void succeeded(SDKResponse<CASession> result) {
                log("Session authorized!");
                requestFileList();
            }

            @Override
            public void failed(SDKException exception) {
                log("Authorization failed", exception.getMessage());
            }
        };

        final boolean forGuest = getIntent().getBooleanExtra(FOR_GUEST_EXTRA, false);

        if (forGuest)
            DigiMeClient.getInstance().authorizeGuest(CallbackActivity.this, cb);
        else
            DigiMeClient.getInstance().authorize(CallbackActivity.this, cb);

        log(R.string.starting);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DigiMeClient.getInstance().getCAAuthManager().onActivityResult(requestCode, resultCode, data);
    }

    public void requestFileList() {
        DigiMeClient.getInstance().getFileList(new SDKCallback<CAFiles>() {
            @Override
            public void succeeded(SDKResponse<CAFiles> result) {
                CAFiles files = result.body;
                if (files.fileIds != null) {
                    requestAccounts();
                    getFileContent(files.fileIds);
                } else {
                    log("Get file list returned no files, called before permission given?");
                }
            }

            @Override
            public void failed(SDKException exception) {
                log("Failed to fetch list", exception.getMessage());
            }
        });
    }

    public void getFileContent(List<String> fileIds) {
        for (final String fileId : fileIds) {
            DigiMeClient.getInstance().getFile(fileId, new SDKCallback<JsonElement>() {
                @Override
                public void succeeded(SDKResponse<JsonElement> result) {
                    log("Content retrieved for file " + fileId, result.body.toString());
                }

                @Override
                public void failed(SDKException exception) {
                    log("Failed to retrieve file content for file: " + fileId, exception.toString());
                }
            });
        }
    }

    public void requestAccounts() {
        log(R.string.fetch_accounts);
        DigiMeClient.getInstance().getAccounts(new SDKCallback<CAAccounts>() {
            @Override
            public void succeeded(SDKResponse<CAAccounts> result) {
                log(String.format(Locale.getDefault(), "Returning data for %d accounts: %s", result.body.accounts.size(), result.body.getAllServiceNames()));
            }

            @Override
            public void failed(SDKException exception) {
                log("Failed to retrieve account details for session", exception.toString());
            }
        });
    }
}
