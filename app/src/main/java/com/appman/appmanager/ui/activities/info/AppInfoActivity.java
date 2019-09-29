package com.appman.appmanager.ui.activities.info;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.ui.activities.base.BaseActivity;
import com.appman.appmanager.ui.activities.main.ActivityNavigationView;
import com.appman.appmanager.util.AppPreferences;
import com.appman.appmanager.util.FileUtil;
import com.appman.appmanager.util.PrintLog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.Set;

public class AppInfoActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private static final String TAG = "AppInfoActivity";

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mTitleContainer;

    private AppBarLayout appBarLayout;
    private FloatingActionButton fabFav;
    private ImageView icon;

    private AppPreferences appPreferences;
    private AppInfo appInfo;
    private TextView version, tvAppName;

    private Set<String> appsFavorite;
    private int UNINSTALL_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_app_info);
        appPreferences = AppManagerController.getInstance().getAppPreferences();
        getIntentData();
        initializeToolbar();
        initializeViews();
        initializeListeners();
        setAppInfo();
    }

    @Override
    protected void initializeToolbar() {
        super.initializeToolbar();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        mTitleContainer = findViewById(R.id.info_linearlayout_title);
        icon = findViewById(R.id.app_icon);
        tvAppName = findViewById(R.id.app_name);
        version = findViewById(R.id.app_version);
        fabFav = findViewById(R.id.fab);
        appBarLayout = findViewById(R.id.app_bar);

        icon.setImageDrawable(appInfo.getIcon());
        tvAppName.setText(appInfo.getName());
        version.setText(appInfo.getVersion());
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PrintLog.getInstance().doPrintInfoLog(TAG, "OK");
                Intent intent = new Intent(baseContext, ActivityNavigationView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                startActivity(intent);
            } else if (resultCode == RESULT_CANCELED) {
                PrintLog.getInstance().doPrintInfoLog(TAG, "CANCEL");
            }
        }
    }

    private void initializeListeners() {
        appBarLayout.addOnOffsetChangedListener(this);

        fabFav.setOnClickListener(view -> {
            if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), appsFavorite)) {
                appsFavorite.remove(appInfo.getAPK());
                appPreferences.setFavoriteApps(appsFavorite);
                LocalBroadcastManager.getInstance(baseContext)
                        .sendBroadcast(new Intent(AppConstants.ACTION_APP_REMOVED_FROM_FAVORITES_CALLBACK));
            } else {
                appsFavorite.add(appInfo.getAPK());
                appPreferences.setFavoriteApps(appsFavorite);
                LocalBroadcastManager.getInstance(baseContext)
                        .sendBroadcast(new Intent(AppConstants.ACTION_APP_PUT_TO_FAVORITES_CALLBACK));
            }
            FileUtil.getInstance().setAppFavorite(baseContext, fabFav,
                    FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), appsFavorite));
        });
    }

    private void getIntentData() {
        String appName = getIntent().getStringExtra("app_name");
        String appApk = getIntent().getStringExtra("app_apk");
        String appVersion = getIntent().getStringExtra("app_version");
        String appSource = getIntent().getStringExtra("app_source");
        String appData = getIntent().getStringExtra("app_data");
        String appSize = getIntent().getStringExtra("app_size");
        Bitmap bitmap = getIntent().getParcelableExtra("app_icon");
        Drawable appIcon = new BitmapDrawable(getResources(), bitmap);
        Boolean appIsSystem = getIntent().getExtras().getBoolean("app_isSystem");

        appInfo = new AppInfo(appName, appApk, appVersion, appSource, appData, appSize, appIcon, appIsSystem);
        appsFavorite = appPreferences.getFavoriteApps();
    }

    private void setAppInfo() {

        ImageView icon_googleplay = findViewById(R.id.app_googleplay);

        TextView apk = findViewById(R.id.app_apk);
        CardView googleplay = findViewById(R.id.id_card);
        CardView start = findViewById(R.id.start_card);
        CardView extract = findViewById(R.id.extract_card);
        CardView uninstall = findViewById(R.id.uninstall_card);

        apk.setText(appInfo.getAPK());

        // Setting fav app
        if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), appsFavorite)) {
            fabFav.setImageResource(R.drawable.ic_star_white);
        }

        if (appInfo.isSystem()) {
            icon_googleplay.setVisibility(View.GONE);
            start.setVisibility(View.GONE);
        } else {
            googleplay.setOnClickListener(v -> FileUtil.getInstance().goToGooglePlay(baseContext, appInfo.getAPK()));

            start.setOnClickListener(v -> {
                try {
                    if (appInfo.getName().equalsIgnoreCase("AppManager")){
                        PrintLog.getInstance().doPrintErrorLog(TAG, "This app can't be opened, because this app is already in its working state.");
                    }
                    else{
                        Intent intent = getPackageManager().getLaunchIntentForPackage(appInfo.getAPK());
                        startActivity(intent);
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            });

            uninstall.setOnClickListener(v -> {
                if (appInfo.getName().equalsIgnoreCase("AppManager")){
                    PrintLog.getInstance().doPrintErrorLog(TAG, "This app can't be uninstalled, because this app is in its working state. Try uninstalling manually.");
                }else{
                    Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                    intent.setData(Uri.parse("package:" + appInfo.getAPK()));
                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                    startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
                }
            });

            extract.setOnClickListener(v -> extractAppInBackground());
        }
    }

    private void extractAppInBackground() {
        new ExtractFileInBackground(AppInfoActivity.this, appInfo).execute();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(tvAppName, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(tvAppName, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    private static class ExtractFileInBackground extends AsyncTask<Void, Integer, Boolean> {
        private WeakReference<AppInfoActivity> weakReference;
        private AppInfo appInfo;
        private ProgressDialog alertDialog;

        private ExtractFileInBackground(AppInfoActivity activity,
                                       AppInfo app) {
            this.weakReference = new WeakReference<>(activity);
            this.appInfo = app;
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
                Snackbar.make(weakReference.get().fabFav,
                        String.format(weakReference.get().getApplicationContext().getResources()
                        .getString(R.string.dialog_saved_description), appInfo.getName(),
                        FileUtil.getInstance().getAPKFilename(appInfo)), Snackbar.LENGTH_LONG)
                        .setAction(weakReference.get().getApplicationContext().getResources().getString(R.string.button_undo), v -> {
                            boolean result = FileUtil.getInstance().getOutputFilename(appInfo).delete();
                            PrintLog.getInstance().doPrintInfoLog(TAG, "Result of undo: " + result);
                        });
            } else {
                Snackbar.make(weakReference.get().fabFav,
                        weakReference.get().getApplicationContext()
                                .getResources().getString(R.string.dialog_extract_fail)
                                + weakReference.get().getApplicationContext().getResources().getString(R.string.dialog_extract_fail_description),
                        Snackbar.LENGTH_LONG);
            }
        }
    }
}
