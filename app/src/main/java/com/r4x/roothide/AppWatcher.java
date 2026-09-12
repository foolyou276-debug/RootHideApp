package com.r4x.roothide;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppWatcher extends Service {

    private static final String CHANNEL_ID = "r4x_watcher";
    private static final int CHECK_INTERVAL = 3000; // 3 seconds

    // Target apps to watch
    private static final Set<String> TARGET_APPS = new HashSet<>(Arrays.asList(
        "com.dts.freefiremax",
        "com.roblox.client",
        "com.winzo.gold",
        "com.winzo",
        "com.rupiyo",
        "com.cashbunny",
        "com.gamengig",
        "com.android.vending",
        "com.google.android.gms"
    ));

    private Handler handler;
    private Set<String> hiddenApps = new HashSet<>();
    private boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(2, buildNotification());
        running = true;
        startWatching();
        return START_STICKY;
    }

    private void startWatching() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                checkRunningApps();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }, CHECK_INTERVAL);
    }

    private void checkRunningApps() {
        new Thread(() -> {
            // Get running processes
            String result = NativeHelper.runRoot("ps -A | grep -E 'freefiremax|roblox|winzo|rupiyo|cashbunny|gamengig'");

            for (String pkg : TARGET_APPS) {
                if (result.contains(pkg) && !hiddenApps.contains(pkg)) {
                    // App just launched - apply hide
                    NativeHelper.applyAllHide();
                    hiddenApps.add(pkg);
                } else if (!result.contains(pkg)) {
                    hiddenApps.remove(pkg);
                }
            }
        }).start();
    }

    private Notification buildNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("R4X App Watcher")
            .setContentText("Watching for app launches...")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "R4X App Watcher",
            NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
