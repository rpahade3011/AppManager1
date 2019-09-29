package com.appman.appmanager.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.runtime.AppRuntimeException;
import com.appman.appmanager.util.AppPreferences;
import com.appman.appmanager.util.PrintLog;
import com.appman.appmanager.util.TypefaceUtil;

public class AppManagerController extends Application {
    private static final String TAG = "AppManagerController";
    private static AppManagerController INSTANCE;
    private AppPreferences appPreferences;
    private Activity currentActivity = null;

    public AppManagerController() { INSTANCE = this; }

    public static AppManagerController getInstance() {
        return INSTANCE;
    }

    public AppPreferences getAppPreferences() {
         return appPreferences;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
        try {
            PrintLog.getInstance().doPrintInfoLog(TAG,
                    "*********** Current activity *************"
                            + currentActivity.getLocalClassName());
        } catch (Exception e) {
            Log.e(TAG, "Exception occurred while setting current activity");
        }
    }

    private void clearActivityReferences() {
        Activity currActivity = getCurrentActivity();
        if (this.equals(currActivity)) {
            setCurrentActivity(null);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PrintLog.getInstance().doPrintInfoLog(TAG, "inside onCreate()");
        INSTANCE.registerActivityLifecycleCallbacks(mActivityLifeCycleCallback);
        appPreferences = new AppPreferences(this);
        initTypeface();

        // Registering runtime exceptions
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    /**
     * Sets default typeface throughout the application
     */
    private void initTypeface() {
        TypefaceUtil.getInstance().overrideFont(this,
                "SERIF", AppConstants.FONT_PATH);
        TypefaceUtil.getInstance().overrideFont(this,
                "MONOSPACE", AppConstants.FONT_PATH);
        TypefaceUtil.getInstance().overrideFont(this,
                "DEFAULT", AppConstants.FONT_PATH);
    }

    private void handleUncaughtException(final Thread thread, final Throwable e) {
        new AppRuntimeException(thread, e, getCurrentActivity());
    }

    private final ActivityLifecycleCallbacks mActivityLifeCycleCallback = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) { }

        @Override
        public void onActivityResumed(Activity activity) {
            setCurrentActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
             clearActivityReferences();
        }

        @Override
        public void onActivityStopped(Activity activity) { }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

        @Override
        public void onActivityDestroyed(Activity activity) {
            clearActivityReferences();
        }
    };
}
