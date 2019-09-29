package com.appman.appmanager.util;

import android.util.Log;

public class PrintLog {
    private static PrintLog sInstance = null;

    private PrintLog() {}

    public static PrintLog getInstance() {
        if (sInstance == null) {
            sInstance = new PrintLog();
        }
        return sInstance;
    }

    public void doPrintDebugLog(String LOG_TAG, String message) {
        Log.d(LOG_TAG, message);
    }

    public void doPrintInfoLog(String LOG_TAG, String message) {
        Log.i(LOG_TAG, message);
    }

    public void doPrintWarnLog(String LOG_TAG, String message) {
        Log.w(LOG_TAG, message);
    }

    public void doPrintErrorLog(String LOG_TAG, String message) {
        Log.e(LOG_TAG, message);
    }
}
