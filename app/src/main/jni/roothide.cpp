#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/system_properties.h>
#include <unistd.h>
#include <sys/mount.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <cstring>
#include <cstdlib>

#define TAG "R4X_RootHide"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Set system property using __system_property_set
int setprop(const char* key, const char* value) {
    int ret = __system_property_set(key, value);
    LOGI("setprop %s = %s (ret=%d)", key, value, ret);
    return ret;
}

// Hide file using bind mount
int hide_file(const char* path, const char* replacement) {
    struct stat st;
    if (stat(path, &st) != 0) return 0; // File doesn't exist
    int ret = mount(replacement, path, nullptr, MS_BIND, nullptr);
    LOGI("hide_file %s (ret=%d)", path, ret);
    return ret;
}

// Run shell command as root
int run_root(const char* cmd) {
    std::string full = std::string("su -c '") + cmd + "'";
    int ret = system(full.c_str());
    LOGI("run_root: %s (ret=%d)", cmd, ret);
    return ret;
}

extern "C" {

// Apply all props
JNIEXPORT jint JNICALL
Java_com_r4x_roothide_NativeHelper_applyProps(JNIEnv* env, jobject) {
    LOGI("Applying Samsung A54 5G props...");

    // Samsung Galaxy A54 5G - Android 14
    setprop("ro.product.model", "SM-A546E");
    setprop("ro.product.brand", "samsung");
    setprop("ro.product.name", "a54xnsxx");
    setprop("ro.product.device", "a54x");
    setprop("ro.product.manufacturer", "samsung");
    setprop("ro.product.board", "a54x");
    setprop("ro.build.fingerprint",
        "samsung/a54xnsxx/a54x:14/UP1A.231005.007/A546EXXU7DXD1:user/release-keys");
    setprop("ro.build.tags", "release-keys");
    setprop("ro.build.type", "user");
    setprop("ro.build.version.release", "14");
    setprop("ro.build.version.sdk", "34");
    setprop("ro.build.version.security_patch", "2024-04-01");
    setprop("ro.debuggable", "0");
    setprop("ro.secure", "1");
    setprop("ro.adb.secure", "1");
    setprop("ro.kernel.qemu", "0");
    setprop("ro.kernel.qemu.gles", "0");
    setprop("ro.hardware", "qcom");
    setprop("ro.hardware.egl", "mali");
    setprop("ro.board.platform", "sm7325");
    setprop("ro.boot.verifiedbootstate", "green");
    setprop("ro.boot.flash.locked", "1");
    setprop("ro.vphone.version", "");
    setprop("ro.vmos.version", "");
    setprop("ro.virtual.device", "0");
    setprop("service.adb.root", "0");
    setprop("samsung.build.platform_version", "14");

    LOGI("Props applied!");
    return 0;
}

// Hide root files
JNIEXPORT jint JNICALL
Java_com_r4x_roothide_NativeHelper_hideRootFiles(JNIEnv* env, jobject) {
    LOGI("Hiding root files...");

    // Hide su binaries
    const char* su_paths[] = {
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/system/xbin/daemonsu",
        "/system/xbin/busybox",
        nullptr
    };

    for (int i = 0; su_paths[i]; i++) {
        hide_file(su_paths[i], "/system/bin/false");
    }

    // Hide root manager artifacts
    const char* hide_paths[] = {
        "/data/adb/magisk",
        "/data/adb/modules",
        "/data/adb/apatch",
        "/sbin/.magisk",
        "/cache/magisk.log",
        nullptr
    };

    for (int i = 0; hide_paths[i]; i++) {
        struct stat st;
        if (stat(hide_paths[i], &st) == 0) {
            mount("/dev/null", hide_paths[i], nullptr, MS_BIND, nullptr);
        }
    }

    // SELinux enforcing
    run_root("setenforce 1");

    LOGI("Root files hidden!");
    return 0;
}

// Get prop value
JNIEXPORT jstring JNICALL
Java_com_r4x_roothide_NativeHelper_getProp(JNIEnv* env, jobject, jstring jkey) {
    const char* key = env->GetStringUTFChars(jkey, nullptr);
    char value[PROP_VALUE_MAX];
    __system_property_get(key, value);
    env->ReleaseStringUTFChars(jkey, key);
    return env->NewStringUTF(value);
}

// Grant permissions to package
JNIEXPORT jint JNICALL
Java_com_r4x_roothide_NativeHelper_grantPermissions(JNIEnv* env, jobject, jstring jpkg) {
    const char* pkg = env->GetStringUTFChars(jpkg, nullptr);

    const char* perms[] = {
        "android.permission.READ_PHONE_STATE",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.GET_ACCOUNTS",
        nullptr
    };

    for (int i = 0; perms[i]; i++) {
        std::string cmd = std::string("pm grant ") + pkg + " " + perms[i];
        run_root(cmd.c_str());
    }

    env->ReleaseStringUTFChars(jpkg, pkg);
    return 0;
}

} // extern "C"
