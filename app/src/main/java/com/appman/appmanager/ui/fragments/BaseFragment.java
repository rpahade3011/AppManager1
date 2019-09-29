package com.appman.appmanager.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appman.appmanager.R;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.util.FileUtil;
import com.yalantis.phoenix.PullToRefreshView;

import java.lang.ref.WeakReference;

public abstract class BaseFragment extends Fragment {

    private BroadcastReceiver mAppBroadCastReceiver;
    private boolean mBroadcastReceiverRegistered = false;

    public Context activityContext;
    public LinearLayout noResults;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activityContext = getActivity().getApplicationContext();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAppBroadCastReceiver = null;
    }

    protected void registerBroadcastReceiver(IntentFilter intentFilter) {
        mAppBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onBroadcastReceived(context, intent);
            }
        };
        mBroadcastReceiverRegistered = true;
        LocalBroadcastManager.getInstance(activityContext).registerReceiver(mAppBroadCastReceiver, intentFilter);
    }

    protected void unRegisterBroadcastReceiver() {
        if (mAppBroadCastReceiver != null && mBroadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(activityContext).unregisterReceiver(mAppBroadCastReceiver);
            mBroadcastReceiverRegistered = false;
        }
    }

    protected void onBroadcastReceived(Context context, Intent intent) {
    }

    protected void initializeViews(View viewItem) { }

    protected void showErrorLayout(Boolean result) { }

    protected void navigateToFragment(Fragment fragmentInstance, String fragmentTag) { }

    protected void removeAttachedFragment(Fragment fragmentInstance, String fragmentTag) { }

    protected void getAllAppsFromDevice() { }

    protected void setAdapter() { }

    protected void setPullToRefreshView(PullToRefreshView pullToRefreshView) { }

    protected void getIntentData() {}

    protected void extractAppInBackground(Activity activity, AppInfo appInfo) {
        new ExtractFileInBackground(activity, appInfo).execute();
    }

    private static class ExtractFileInBackground extends AsyncTask<Void, Integer, Boolean> {

        private WeakReference<Activity> weakReference;
        private AppInfo appInfo;
        private ProgressDialog alertDialog;

        public ExtractFileInBackground(Activity activity, AppInfo info) {
            this.weakReference = new WeakReference<>(activity);
            this.appInfo = info;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            alertDialog = new ProgressDialog(weakReference.get());
            alertDialog.setTitle(String.format(weakReference.get().getResources()
                    .getString(R.string.dialog_saving), appInfo.getName()));
            alertDialog.setMessage(weakReference.get().getResources()
                    .getString(R.string.dialog_saving_description));
            alertDialog.setProgress(0);
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            int appSize = appInfo.getAPK().length();
            publishProgress(alertDialog.getProgress() * 100 / appSize);
            return FileUtil.getInstance().copyFile(appInfo);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            alertDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            alertDialog.dismiss();
            if (aBoolean) {
                Toast success = Toast.makeText(weakReference.get().getApplicationContext(),
                        String.format(weakReference.get().getApplicationContext().getResources()
                                .getString(R.string.dialog_saved_description), appInfo.getName(),
                        FileUtil.getInstance().getAPKFilename(appInfo)), Toast.LENGTH_SHORT);
                success.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
                success.show();
            } else {
                Toast fail = Toast.makeText(weakReference.get().getApplicationContext(),
                        weakReference.get().getApplicationContext()
                                .getResources().getString(R.string.dialog_extract_fail)
                                + weakReference.get().getApplicationContext().getResources().getString(R.string.dialog_extract_fail_description), Toast.LENGTH_SHORT);
                fail.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
                fail.show();
            }
        }
    }
}
