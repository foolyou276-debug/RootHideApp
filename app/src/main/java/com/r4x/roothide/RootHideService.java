package com.r4x.roothide;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RootHideService extends Service {

    private static final String CHANNEL_ID = "r4x_hide";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, buildNotification());

        // Apply hide in background
        new Thread(() -> {
            NativeHelper.applyAllHide();
            NativeHelper.grantAllPermissions();
        }).start();

        return START_STICKY;
    }

    private Notification buildNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("R4X Root Hide")
            .setContentText("Protection Active ✅")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "R4X Root Hide",
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Root hide service running");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
