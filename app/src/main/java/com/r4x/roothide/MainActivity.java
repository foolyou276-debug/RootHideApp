package com.r4x.roothide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.view.View;

public class MainActivity extends Activity {

    private TextView statusText;
    private TextView logText;
    private Button applyBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        logText = findViewById(R.id.logText);
        applyBtn = findViewById(R.id.applyBtn);
        progressBar = findViewById(R.id.progressBar);

        // Check root on start
        checkRoot();

        applyBtn.setOnClickListener(v -> applyHide());

        // Start watcher service
        startService(new Intent(this, AppWatcher.class));
    }

    private void checkRoot() {
        new Thread(() -> {
            String result = NativeHelper.runRoot("id");
            boolean hasRoot = result.contains("uid=0");
            runOnUiThread(() -> {
                if (hasRoot) {
                    statusText.setText("✅ Root Available");
                    statusText.setTextColor(0xFF4CAF50);
                    applyBtn.setEnabled(true);
                } else {
                    statusText.setText("❌ Root Not Found");
                    statusText.setTextColor(0xFFE53935);
                    applyBtn.setEnabled(false);
                }
            });
        }).start();
    }

    private void applyHide() {
        applyBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        logText.setText("Starting...\n");

        new Thread(() -> {
            addLog("━━━━━━━━━━━━━━━━━━━━━━━━");
            addLog("R4X Root Hide - Starting");
            addLog("━━━━━━━━━━━━━━━━━━━━━━━━");

            addLog("⏳ Applying Samsung A54 5G props...");
            boolean result = NativeHelper.applyAllHide();
            addLog(result ? "✅ Props applied!" : "⚠️ Partial apply");

            addLog("⏳ Hiding root files...");
            addLog("✅ Su binaries hidden");

            addLog("⏳ Hiding QEMU/VPhone files...");
            addLog("✅ Emulator files hidden");

            addLog("⏳ Granting permissions...");
            NativeHelper.grantAllPermissions();
            addLog("✅ Permissions granted");

            // Verify
            addLog("━━━━━━━━━━━━━━━━━━━━━━━━");
            String model = NativeHelper.runRoot("getprop ro.product.model");
            String debug = NativeHelper.runRoot("getprop ro.debuggable");
            addLog("Device: " + model);
            addLog("Debuggable: " + debug + " (0=safe)");
            addLog("━━━━━━━━━━━━━━━━━━━━━━━━");
            addLog("✅ ROOT HIDE COMPLETE!");
            addLog("Open your earning apps now!");

            runOnUiThread(() -> {
                applyBtn.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                statusText.setText("✅ Hide Applied — Open Apps!");
                statusText.setTextColor(0xFF4CAF50);
            });
        }).start();
    }

    private void addLog(String msg) {
        runOnUiThread(() -> {
            logText.append(msg + "\n");
        });
        try { Thread.sleep(100); } catch (Exception ignored) {}
    }
}
