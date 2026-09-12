package com.r4x.roothide;

public class NativeHelper {

    static {
        System.loadLibrary("roothide");
    }

    // Apply Samsung A54 5G props
    public native int applyProps();

    // Hide root files via bind mount
    public native int hideRootFiles();

    // Get system property
    public native String getProp(String key);

    // Grant permissions to package
    public native int grantPermissions(String packageName);

    // Run shell command as root
    public static String runRoot(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            p.waitFor();
            byte[] out = p.getInputStream().readAllBytes();
            return new String(out).trim();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Apply all hiding
    public static boolean applyAllHide() {
        try {
            // Props via shell (more reliable in VPhoneGaGa)
            String[] props = {
                "ro.product.model=SM-A546E",
                "ro.product.brand=samsung",
                "ro.product.manufacturer=samsung",
                "ro.product.device=a54x",
                "ro.product.name=a54xnsxx",
                "ro.build.fingerprint=samsung/a54xnsxx/a54x:14/UP1A.231005.007/A546EXXU7DXD1:user/release-keys",
                "ro.build.tags=release-keys",
                "ro.build.type=user",
                "ro.build.version.release=14",
                "ro.build.version.sdk=34",
                "ro.build.version.security_patch=2024-04-01",
                "ro.debuggable=0",
                "ro.secure=1",
                "ro.adb.secure=1",
                "ro.kernel.qemu=0",
                "ro.hardware=qcom",
                "ro.hardware.egl=mali",
                "ro.boot.verifiedbootstate=green",
                "ro.boot.flash.locked=1",
                "ro.vphone.version=",
                "ro.virtual.device=0",
                "service.adb.root=0"
            };

            // Try resetprop first, fallback to setprop
            for (String prop : props) {
                String[] kv = prop.split("=", 2);
                String key = kv[0];
                String val = kv.length > 1 ? kv[1] : "";
                // Try resetprop
                String result = runRoot("resetprop " + key + " '" + val + "' 2>/dev/null || setprop " + key + " '" + val + "'");
            }

            // Hide su binaries
            String[] suPaths = {
                "/system/xbin/su",
                "/system/bin/su",
                "/sbin/su",
                "/system/xbin/daemonsu",
                "/system/xbin/busybox"
            };
            for (String path : suPaths) {
                runRoot("test -f " + path + " && mount --bind /system/bin/false " + path);
            }

            // Hide artifacts
            String[] artifacts = {
                "/data/adb/magisk",
                "/data/adb/modules",
                "/data/adb/apatch",
                "/sbin/.magisk",
                "/cache/magisk.log"
            };
            for (String art : artifacts) {
                runRoot("test -e " + art + " && mount --bind /dev/null " + art);
            }

            // Hide QEMU files
            runRoot("test -e /dev/qemu_pipe && mount --bind /dev/null /dev/qemu_pipe");
            runRoot("test -e /dev/socket/qemud && mount --bind /dev/null /dev/socket/qemud");
            runRoot("test -e /dev/goldfish_pipe && mount --bind /dev/null /dev/goldfish_pipe");

            // SELinux
            runRoot("setenforce 1");

            // DNS
            runRoot("setprop net.dns1 8.8.8.8");
            runRoot("setprop net.dns2 1.1.1.1");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Grant permissions to all earning apps
    public static void grantAllPermissions() {
        String[] packages = {
            "com.dts.freefiremax",
            "com.roblox.client",
            "com.google.android.gms",
            "com.android.vending",
            "com.google.android.youtube",
            "com.winzo.gold",
            "com.winzo",
            "com.rupiyo",
            "in.startv.hotstar",
            "com.gametion.ludokinggame",
            "com.cashbunny",
            "com.gamengig",
        };

        String[] perms = {
            "android.permission.READ_PHONE_STATE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.GET_ACCOUNTS",
        };

        for (String pkg : packages) {
            for (String perm : perms) {
                runRoot("pm grant " + pkg + " " + perm + " 2>/dev/null");
            }
        }
    }
}
