/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View authorizeWithListener = findViewById(R.id.authorize_with_listener);
        authorizeWithListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListenerActivity.class));
            }
        });

        View authorizeWithCallback = findViewById(R.id.authorize_with_callback);
        authorizeWithCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CallbackActivity.class));
            }
        });

        View authorizeGuestWithListener = findViewById(R.id.authorize_guest_with_listener);
        authorizeGuestWithListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListenerActivity.class);
                intent.putExtra(ListenerActivity.FOR_GUEST_EXTRA, true);
                startActivity(intent);
            }
        });

        View authorizeGuestWithCallback = findViewById(R.id.authorize_guest_with_callback);
        authorizeGuestWithCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CallbackActivity.class);
                intent.putExtra(CallbackActivity.FOR_GUEST_EXTRA, true);
                startActivity(intent);
            }
        });
    }
}
