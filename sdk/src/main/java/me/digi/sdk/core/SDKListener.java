/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core;

import com.google.gson.JsonElement;

import me.digi.sdk.core.entities.CAAccounts;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.internal.AuthorizationException;
import me.digi.sdk.core.session.CASession;

public interface SDKListener {

    void sessionCreated(CASession session);
    void sessionCreateFailed(SDKException reason);

    void authorizeSucceeded(CASession session);
    void authorizeDenied(AuthorizationException reason);
    void authorizeFailedWithWrongRequestCode();

    void clientRetrievedFileList(CAFiles files);
    void clientFailedOnFileList(SDKException reason);

    void contentRetrievedForFile(@SuppressWarnings("UnusedParameters") String fileId, CAFileResponse content);
    void jsonRetrievedForFile(@SuppressWarnings("UnusedParameters") String fileId, JsonElement content);
    void contentRetrieveFailed(String fileId, SDKException reason);

    void accountsRetrieved(CAAccounts accounts);
    void accountsRetrieveFailed(SDKException reason);
}
