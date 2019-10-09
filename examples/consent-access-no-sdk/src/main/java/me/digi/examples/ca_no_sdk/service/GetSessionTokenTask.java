/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import me.digi.examples.ca_no_sdk.service.models.SessionKeyCreateResponse;
import me.digi.examples.ca_no_sdk.service.models.SessionTokenBody;

import retrofit2.Response;

public class GetSessionTokenTask extends AsyncTask<GetSessionTokenTask.GetSessionTokenTaskParams, Void, Response<SessionKeyCreateResponse>> {
    private Listener listener;

    @Override
    protected Response<SessionKeyCreateResponse> doInBackground(GetSessionTokenTaskParams... getSessionTokenTaskParams) {
        GetSessionTokenTaskParams params = getSessionTokenTaskParams[0];
        this.listener = params.getListener();
        try {
            return params.getPermissionService().getSessionToken(params.getSessionTokenBody()).execute();
        } catch (final Exception e) {
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        listener.sessionTokenTaskFailed(e);
                    }
                });
            }
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Response<SessionKeyCreateResponse> response) {
        if (listener != null) {
            listener.sessionTokenTaskComplete(response);
        }
    }

    public interface Listener {
        void sessionTokenTaskComplete(Response<SessionKeyCreateResponse> response);
        void sessionTokenTaskFailed(Exception e);
    }

    public static class GetSessionTokenTaskParams {
        private PermissionService permissionService;
        private SessionTokenBody sessionTokenBody;
        private Listener listener;

        public GetSessionTokenTaskParams(PermissionService permissionService, SessionTokenBody sessionTokenBody, Listener listener) {
            this.permissionService = permissionService;
            this.sessionTokenBody = sessionTokenBody;
            this.listener = listener;
        }

        public PermissionService getPermissionService() {
            return permissionService;
        }

        public SessionTokenBody getSessionTokenBody() {
            return sessionTokenBody;
        }

        public Listener getListener() {
            return listener;
        }
    }
}
