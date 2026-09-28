// PermissionHelper.java
package com.example.lifefit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    // Request codes
    public static final int REQUEST_SMS_PERMISSION = 100;
    public static final int REQUEST_CONTACTS_PERMISSION = 101;

    // Check if permission is granted
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    // Request permission
    public static void requestSmsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.SEND_SMS},
                REQUEST_SMS_PERMISSION);
    }

    public static void requestContactsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CONTACTS_PERMISSION);
    }

    // Check if we should show permission rationale
    public static boolean shouldShowSmsRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.SEND_SMS);
    }

    public static boolean shouldShowContactsRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_CONTACTS);
    }
}