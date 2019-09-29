package com.appman.appmanager.util;

import android.Manifest;
import android.content.pm.PackageManager;

public class PermissionUtil {
    private static PermissionUtil sInstance = null;

    public static String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int ALL_PERM_REQ_CODE = 1111;

    public static PermissionUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PermissionUtil();
        }
        return sInstance;
    }

    public boolean verifyResult(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
