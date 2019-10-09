/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal.ipc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import me.digi.sdk.core.DigiMeBaseAuthManager;
import me.digi.sdk.core.DigiMeClient;


public class InstallReceiver extends BroadcastReceiver {
    private static final String TAG = InstallReceiver.class.getSimpleName();
    private static volatile InstallReceiver singleton;

    private static final String CA_FIRST_INSTALL_MODE = "me.digi.messaging.ca_launch";
    private static final String CA_FIRST_INSTALL_MESSAGE = "package_launched";
    private static final String CA_FIRST_INSTALL_PACKAGE = "me.digi.messaging.package_name";

    private static final String CA_BROADCAST_RECEIVER = "me.digi.messaging.CA_RECEIVER";
    private static final String CA_BROADCAST_FIRST_DATA_PING = "me.digi.messages.data_available";
    // TODO implement ping in digi.me app
    private static final String CA_BROADCAST_ONBOARDED_PING = "me.digi.messages.onboarded";

    private InstallReceiver()
    {
        Log.d(TAG, "InstallReceiver started!");
    }

    public static InstallReceiver getInstance() {
        if (singleton == null) {
            synchronized (InstallReceiver.class) {
                singleton = new InstallReceiver();
            }
        }
        return singleton;
    }

    public static void registerForMessages() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        DigiMeClient.getApplicationContext().registerReceiver(getInstance(), filter);
        IntentFilter pingFilter = new IntentFilter(CA_BROADCAST_RECEIVER);
        DigiMeClient.getApplicationContext().registerReceiver(getInstance(), pingFilter);
    }

    public static void unregisterReceiver() {
        try {
            DigiMeClient.getApplicationContext().unregisterReceiver(getInstance());
        } catch (Exception ex) {
            Log.d(TAG, "Receiver already unregistered!", ex);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        routeMessage(context, intent);
    }

    private void routeMessage(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_PACKAGE_ADDED:
                handleClientInstall(context, intent);
                break;
            case CA_BROADCAST_RECEIVER:
                processBroadcast(context, intent);
                break;
        }

    }

    private void handleClientInstall(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getData() == null) return;
        String installedPackage = intent.getData().getSchemeSpecificPart();
        if (installedPackage == null || !installedPackage.equals(DigiMeBaseAuthManager.DIGI_ME_PACKAGE_ID)) return;

        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(installedPackage);
        if (launchIntent != null) {
            launchIntent.putExtra(CA_FIRST_INSTALL_MODE, CA_FIRST_INSTALL_MESSAGE);
            String appPackage = DigiMeClient.getApplicationContext().getPackageName();
            if (!TextUtils.isEmpty(appPackage)) {
                launchIntent.putExtra(CA_FIRST_INSTALL_PACKAGE, appPackage);
            }
            context.startActivity(launchIntent);
        }

    }

    private void processBroadcast(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getBooleanExtra(CA_BROADCAST_FIRST_DATA_PING, false)) {
            Log.d(TAG, "Action received: " + intent.getAction());
            DigiMeClient.getInstance().getCAAuthManager().protocolResolved();
        } else if (intent.getBooleanExtra(CA_BROADCAST_ONBOARDED_PING, false)) {
            Log.d(TAG, "Action received: " + intent.getAction());
            DigiMeClient.getInstance().getPostboxAuthManager().protocolResolved();
        }
    }
}
