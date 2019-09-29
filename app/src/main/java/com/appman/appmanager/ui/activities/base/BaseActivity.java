package com.appman.appmanager.ui.activities.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseActivity extends AppCompatActivity {
    private BroadcastReceiver mAppBroadCastReceiver;
    private boolean mBroadcastReceiverRegistered = false;
    public Boolean doubleBackToExitPressedOnce = false;

    public Context baseContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.baseContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void registerBroadcastReceiver(IntentFilter intentFilter) {
        mAppBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onBroadcastReceived(context, intent);
            }
        };
        mBroadcastReceiverRegistered = true;
        LocalBroadcastManager.getInstance(this).registerReceiver(mAppBroadCastReceiver, intentFilter);
    }

    protected void unRegisterBroadcastReceiver() {
        if (mAppBroadCastReceiver != null && mBroadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mAppBroadCastReceiver);
            mBroadcastReceiverRegistered = false;
        }
    }

    protected void onBroadcastReceived(Context context, Intent intent) {}

    protected void initializeToolbar() {}

    protected void setToolbarTypeface(Toolbar toolbar) {}

    protected void initializeViews() {}

    protected void navigateToScreen(Intent intentObject) {}

    protected void loadFragment(Fragment fragmentInstance) {}

    protected void setToolbarTitle(String title) {}

    protected void getAppsFromDevice() {}

    protected void getFavoriteApps() {}

    protected void showProgressBar(boolean shouldShow) {}

    protected void saveDataToCache(Bundle outState) {}

    protected void restoreDataFromCache(Bundle savedInstanceState) {}

    protected boolean canRestoreDataFromCache(Bundle savedInstanceState) { return false; }
}


