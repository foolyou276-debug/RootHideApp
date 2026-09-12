package com.r4x.roothide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            
            // Start hide service on boot
            Intent serviceIntent = new Intent(context, RootHideService.class);
            context.startForegroundService(serviceIntent);
            
            // Start app watcher
            Intent watcherIntent = new Intent(context, AppWatcher.class);
            context.startForegroundService(watcherIntent);
        }
    }
}
