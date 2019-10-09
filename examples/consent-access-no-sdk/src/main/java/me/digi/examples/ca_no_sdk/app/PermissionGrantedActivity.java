/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.digi.examples.ca_no_sdk.R;
import me.digi.examples.ca_no_sdk.service.GetUserDataTask;
import me.digi.examples.ca_no_sdk.service.PermissionService;
import me.digi.examples.ca_no_sdk.service.models.DataGetResponse;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class PermissionGrantedActivity extends LoadingActivity implements GetUserDataTask.Listener {
    public static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";

    public static Intent getPermissionGrantedActivityIntent(Context context, String sessionToken) {
        Intent intent = new Intent(context, PermissionGrantedActivity.class);
        intent.putExtra(KEY_SESSION_TOKEN, sessionToken);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        activityReturning(data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private CardView permissionsGrantedCard;
    private CardView contentDataCard;
    private TextView totalCount;

    private ContentAdapter contentAdapter;
    private String sessionToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_permission_granted);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        permissionsGrantedCard = (CardView) findViewById(R.id.permissions_granted_card);
        contentDataCard = (CardView) findViewById(R.id.permissions_granted_data_list_card);
        RecyclerView contentRecycler = (RecyclerView) findViewById(R.id.content_recycler);
        totalCount = (TextView) findViewById(R.id.permission_granted_total_count);

        contentAdapter = new ContentAdapter();
        contentRecycler.setLayoutManager(new LinearLayoutManager(this));
        contentRecycler.setAdapter(contentAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            sessionToken = intent.getStringExtra(KEY_SESSION_TOKEN);
            if (!TextUtils.isEmpty(sessionToken)) {
                sendGetDataRequest(sessionToken);
            }
        }
    }

    private void sendGetDataRequest(String sessionToken) {
        // Logging.
        addLog("Calling [query/sessionKey].");

        // Display loading screen.
        contentLoading(getResources().getString(R.string.loading_getting_data));

        // Get user data in background task.
        PermissionService permissionService = ((Application) getApplication()).getPermissionService();
        new GetUserDataTask().execute(new GetUserDataTask.GetUserDataTaskParams(getApplicationContext(), permissionService, sessionToken, null, this));
    }

    @Override
    public void userDataTaskComplete(Response<DataGetResponse> response) {
        contentFinishedLoading();
        if (dealWithResponse("[query/sessionKey]", response, this)) {
            addLog(String.format("[query/sessionKey] successful (%s).", String.valueOf(response.code())));
            showData(response.body());
        }
    }

    private void showData(DataGetResponse dataGetResponse) {
        if (dataGetResponse != null && dataGetResponse.fileList != null) {
            contentDataCard.setVisibility(View.VISIBLE);
            permissionsGrantedCard.setVisibility(View.VISIBLE);

            totalCount.setText(getString(R.string.permission_granted_total_count, dataGetResponse.fileList.size()));
            contentAdapter.updateFiles(dataGetResponse.fileList);
        } else {
            addLog("[query/sessionKey] returned empty response.");
        }
    }

    @Override
    public void userDataTaskFailed(Exception e) {
        if (e instanceof SocketTimeoutException) {
            if (!TextUtils.isEmpty(sessionToken)) {
                addLog("Timeout happened: retrying");
                sendGetDataRequest(sessionToken);
                return;
            }
        }
        contentFinishedLoading();
        throw new RuntimeException("[query/sessionKey] task failed.", e);
    }

    public void fileClicked(TextView fileNameTextView) {
        Intent intent = ViewFileActivity.getViewFileActivityIntent(this, sessionToken, fileNameTextView.getText().toString());
        activityOutgoing(intent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, fileNameTextView, fileNameTextView.getTransitionName());
            ActivityCompat.startActivityForResult(this, intent, 0, options.toBundle());
        } else {
            startActivityForResult(intent, 0);
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.FileViewHolder> {
        private List<String> files;

        public ContentAdapter() {
            this.files = new ArrayList<>();
        }

        public void updateFiles(List<String> files) {
            this.files = files;
            notifyDataSetChanged();
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FileViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            holder.fillView(files.get(position));
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        public class FileViewHolder extends RecyclerView.ViewHolder {
            private TextView fileName;

            public FileViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.content_file_view, parent, false));
                fileName = (TextView) itemView.findViewById(R.id.file_name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileClicked(fileName);
                    }
                });
            }

            public void fillView(String file) {
                fileName.setText(file);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = activityOutgoing(new Intent());
        setResult(RESULT_OK, intent);
        finish();
    }
}
