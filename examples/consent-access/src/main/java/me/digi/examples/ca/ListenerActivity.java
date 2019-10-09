/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonElement;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKListener;
import me.digi.sdk.core.entities.CAAccounts;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.internal.AuthorizationException;
import me.digi.sdk.core.session.CASession;

public class ListenerActivity extends LogActivity implements SDKListener {
    public static final String FOR_GUEST_EXTRA = "for_guest_extra";

    private DigiMeClient dgmClient;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private int allFiles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dgmClient = DigiMeClient.getInstance();

        final boolean forGuest = getIntent().getBooleanExtra(FOR_GUEST_EXTRA, false);

        if (forGuest) {
            dgmClient.addListener(ListenerActivity.this);
            dgmClient.authorizeGuest(ListenerActivity.this, null);
        } else {
            dgmClient.addListener(ListenerActivity.this);
            dgmClient.authorize(ListenerActivity.this, null);
        }

        log(R.string.starting);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dgmClient.getCAAuthManager().onActivityResult(requestCode, resultCode, data);
    }

    /**
     * SDKListener overrides for DiGiMeClient
     */

    @Override
    public void sessionCreated(CASession session) {
        log("Session created with token " + session.getSessionKey());
    }

    @Override
    public void sessionCreateFailed(SDKException reason) {
        log("Session created failed", reason.getMessage());
    }

    @Override
    public void authorizeSucceeded(CASession session) {
        log("Session authorized with token " + session.getSessionKey());
        DigiMeClient.getInstance().getFileList(null);
    }

    @Override
    public void authorizeDenied(AuthorizationException reason) {
        log("Failed to authorize session", reason.getThrowReason().name());
    }

    @Override
    public void authorizeFailedWithWrongRequestCode() {
        log("We received a wrong request code while authorization was in progress!");
    }

    @Override
    public void clientRetrievedFileList(CAFiles files) {
        log(R.string.fetch_accounts);
        //Fetch account metadata
        DigiMeClient.getInstance().getAccounts(null);

        log(String.format(Locale.getDefault(), "Downloaded : %d/%d", 0, files.fileIds.size()));
        allFiles = files.fileIds.size();
        for (final String fileId : files.fileIds) {
            counter.incrementAndGet();
            //Fetch content for returned file IDs
            DigiMeClient.getInstance().getFile(fileId, null);
        }
        String progress = getResources().getQuantityString(R.plurals.files_retrieved, files.fileIds.size(), files.fileIds.size());
        log(progress);
    }

    @Override
    public void clientFailedOnFileList(SDKException reason) {
        log("Failed to retrieve file list", reason.getMessage());
    }

    @Override
    public void contentRetrievedForFile(String fileId, CAFileResponse content) {
    }

    @Override
    public void jsonRetrievedForFile(String fileId, JsonElement content) {
        log("JSON retrieved for file " + fileId, content.toString());
        updateCounters();
    }

    @Override
    public void contentRetrieveFailed(String fileId, SDKException reason) {
        log("Failed to retrieve file content for file: " + fileId, reason.toString());
        failedCount.incrementAndGet();
        updateCounters();
    }

    @Override
    public void accountsRetrieved(CAAccounts accounts) {
        log(String.format(Locale.getDefault(), "Returning data for %d accounts: %s", accounts.accounts.size(), accounts.getAllServiceNames()));
    }

    @Override
    public void accountsRetrieveFailed(SDKException reason) {
        log("Failed to retrieve account details for session", reason.toString());
    }

    private void updateCounters() {
        int current = counter.decrementAndGet();
        log(String.format(Locale.getDefault(), "Downloaded : %d/%d; Failed: %d", allFiles - current, allFiles, failedCount.get()));
    }
}
