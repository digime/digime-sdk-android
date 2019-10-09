/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.examples.postbox;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

public class ActivityUtils {

    public static void showProgressAndDimScreen(View progressSpinner, View dimScreen) {
        showProgressAndDimScreen(progressSpinner, dimScreen, null, 0);
    }

    public static void showProgressAndDimScreen(View progressSpinner, View dimScreen, TextView messageView, int messageResId) {
        progressSpinner.setVisibility(View.VISIBLE);
        if (messageView != null) {
            messageView.animate().cancel();
            if (messageResId != 0) messageView.setText(messageResId);
            messageView.animate().setDuration(500).alpha(1f).start();
        }
        dimScreen.animate().cancel();
        dimScreen.animate().setDuration(500).alpha(1f).start();
        dimScreen.setClickable(true);
    }

    public static void hideProgressAndRemoveDimScreen(View progressSpinner, View dimScreen, @Nullable TextView messageView) {
        progressSpinner.setVisibility(View.GONE);
        if (messageView != null) {
            messageView.animate().cancel();
            messageView.setAlpha(0f);
        }
        dimScreen.animate().cancel();
        dimScreen.setAlpha(0f);
        dimScreen.setClickable(false);
    }
}
