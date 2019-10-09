/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

abstract class LogActivity extends AppCompatActivity {
    protected LogAdapter logAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logAdapter = new LogAdapter(this);

        ListView logList = findViewById(R.id.logList);
        logList.setAdapter(logAdapter);
        logList.setOnItemClickListener(new CopyToClipboardAction("log"));
    }

    public void log(String text) {
        logAdapter.add(getLogDateTime() + ": " + text);
    }

    public void log(String text, String complement) {
        logAdapter.add(getLogDateTime() + ": " + text + "; ▼");
        logAdapter.add(complement);
    }

    public void log(@StringRes int textRes) {
        logAdapter.add(getLogDateTime() + ": " + getString(textRes));
    }

    private String getLogDateTime() {
        return SimpleDateFormat.getTimeInstance().format(new Date());
    }

    private class CopyToClipboardAction implements AdapterView.OnItemClickListener {
        private String label;

        private CopyToClipboardAction(String label) {
            this.label = label;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                TextView textView = (TextView) view;
                String text = textView.getText().toString();
                if (!text.isEmpty()) {
                    copyToClipboard(label, text);
                    showToast("Text has been copied to clipboard!");
                }
            } catch (Exception exception) {
                showToast("Failed to copy into clipboard!");
            }
        }

        private void copyToClipboard(String label, String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            Objects.requireNonNull(clipboard);

            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
