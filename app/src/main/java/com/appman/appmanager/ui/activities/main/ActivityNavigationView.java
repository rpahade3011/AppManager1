package com.appman.appmanager.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.commands.GetAllAppsCommand;
import com.appman.appmanager.commands.GetAllMusicCommand;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.interfaces.IMusicLibraryListener;
import com.appman.appmanager.interfaces.OnGetAllAppsListener;
import com.appman.appmanager.ui.activities.about.ActivityAboutDev;
import com.appman.appmanager.ui.activities.base.BaseActivity;
import com.appman.appmanager.ui.activities.settings.ActivitySettings;
import com.appman.appmanager.ui.fragments.FragmentDeviceInfo;
import com.appman.appmanager.ui.fragments.FragmentError;
import com.appman.appmanager.ui.fragments.FragmentFavApp;
import com.appman.appmanager.ui.fragments.FragmentMusicLibrary;
import com.appman.appmanager.ui.fragments.FragmentSystemApp;
import com.appman.appmanager.ui.fragments.FragmentUserApp;
import com.appman.appmanager.util.AppRater;
import com.appman.appmanager.util.AppUtil;
import com.appman.appmanager.util.FileUtil;
import com.appman.appmanager.util.PrintLog;
import com.claudiodegio.msv.MaterialSearchView;
import com.claudiodegio.msv.OnSearchViewListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityNavigationView extends BaseActivity {

    private static final String LOG_TAG = ActivityNavigationView.class.getSimpleName();
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private TextView installedAppsBadgeView;
    private TextView systemAppsBadgeView;
    private TextView favAppsBadgeView;
    private TextView musicLibraryBadgeView;
    private ImageView imageViewHeader;
    private ShimmerFrameLayout shimmerFrameLayout;
    private MaterialSearchView mSearchView;
    private IMainPresenter mainPresenter;
    private Menu menu;

    // Google Play In-App Update
    private AppUpdateManager mAppUpdateManager = null;
    private MutableLiveData<Boolean> mIsUpdateAvailable = new MutableLiveData<>();
    private AppUpdateInfo updateInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_navigation_view);
        mainPresenter = new MainPresenter(mMainView);
        mainPresenter.notifyUiReady();
        mainPresenter.canRestoreData(savedInstanceState);
    }

    @Override
    protected boolean canRestoreDataFromCache(Bundle savedInstanceState) {
        return savedInstanceState != null
                && savedInstanceState.containsKey(AppConstants.OUT_STATE_INSTALLED_APPS_KEY)
                && savedInstanceState.containsKey(AppConstants.OUT_STATE_SYSTEM_APPS_KEY)
                && savedInstanceState.containsKey(AppConstants.OUT_STATE_FAV_APPS_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPresenter.registerBroadcastReceiver();
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
           if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
               showAppUpdateSnackbar(drawer, R.string.restart_to_update,
                       R.string.action_restart, true);
           }
        });
    }

    @Override
    protected void onDestroy() {
        mainPresenter.unRegisterBroadcastReceiver();
        mAppUpdateManager.unregisterListener(mInstallStateUpdatedListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mainPresenter.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveDataToCache(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        ActivityNavigationView.this.menu = menu;
        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.APP_UPDATE_REQ_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    //  handle user's approval
                    AppUtil.getInstance().makeToast(this, "Downloading latest update");
                    break;
                case RESULT_CANCELED:
                    //  handle user's rejection
                    AppUtil.getInstance().makeToast(this, "Zoinksss, you cancelled to update the latest version");
                    break;
                case ActivityResult.RESULT_IN_APP_UPDATE_FAILED:
                    //  handle update failure
                    AppUtil.getInstance().makeToast(this, "Failed to update latest version");
                    break;
            }
        }
    }

    @Override
    protected void saveDataToCache(Bundle outState) {
        super.saveDataToCache(outState);
        // Saving installed apps list
        if (AppUtil.getInstance().getInstalledAppsList() != null) {
            outState.putParcelableArrayList(AppConstants.OUT_STATE_INSTALLED_APPS_KEY,
                    new ArrayList<Parcelable>(AppUtil.getInstance().getInstalledAppsList()));
        }
        // Saving system apps list
        if (AppUtil.getInstance().getSystemAppsList() != null) {
            outState.putParcelableArrayList(AppConstants.OUT_STATE_SYSTEM_APPS_KEY,
                    new ArrayList<Parcelable>(AppUtil.getInstance().getSystemAppsList()));
        }

        // Saving favorite apps list
        if (AppUtil.getInstance().getFavoriteAppsList() != null) {
            outState.putParcelableArrayList(AppConstants.OUT_STATE_FAV_APPS_KEY,
                    new ArrayList<Parcelable>(AppUtil.getInstance().getFavoriteAppsList()));
        }
    }

    @Override
    protected void restoreDataFromCache(Bundle savedInstanceState) {
        super.restoreDataFromCache(savedInstanceState);
        // Restoring installed apps list
        AppUtil.getInstance()
                .setInstalledApps(savedInstanceState.
                        <AppInfo>getParcelableArrayList(AppConstants.
                                OUT_STATE_INSTALLED_APPS_KEY));
        // Restoring system apps list
        AppUtil.getInstance().setSystemAppsList(savedInstanceState.
                <AppInfo>getParcelableArrayList(AppConstants.OUT_STATE_SYSTEM_APPS_KEY));

        // Restoring favorite apps list
        AppUtil.getInstance().setFavoriteAppsList(savedInstanceState.
                <AppInfo>getParcelableArrayList(AppConstants.OUT_STATE_FAV_APPS_KEY));
    }

    @Override
    protected void initializeToolbar() {
        super.initializeToolbar();
        toolbar = findViewById(R.id.toolbarAboutDev);
        setSupportActionBar(toolbar);
        setToolbarTitle(getResources().getString(R.string.app_name));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        setToolbarTypeface(toolbar);
    }

    @Override
    protected void setToolbarTypeface(Toolbar toolbar) {
        super.setToolbarTypeface(toolbar);
        TextView mToolbarTitleTextView = null;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                mToolbarTitleTextView = (TextView) view;
                if (mToolbarTitleTextView.getText().equals(toolbar.getTitle())) {
                    applyFont(mToolbarTitleTextView);
                }
            }
        }
    }

    @Override
    protected void setToolbarTitle(String title) {
        super.setToolbarTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(OnNavigationItemSelectedListener);

        // Getting header layout
        View navHeaderLayout = navigationView.getHeaderView(0);
        imageViewHeader = navHeaderLayout.findViewById(R.id.imageViewHeader);
        mainPresenter.setHeader();

        // Extracting badge view
        installedAppsBadgeView = (TextView) MenuItemCompat.
                getActionView(navigationView.getMenu().findItem(R.id.nav_installed_apps));

        systemAppsBadgeView = (TextView) MenuItemCompat.
                getActionView(navigationView.getMenu().findItem(R.id.nav_system_apps));

        favAppsBadgeView = (TextView) MenuItemCompat.
                getActionView(navigationView.getMenu().findItem(R.id.nav_favorites));

        musicLibraryBadgeView = (TextView) MenuItemCompat
                .getActionView(navigationView.getMenu().findItem(R.id.nav_music_library));

        mSearchView = findViewById(R.id.msv_search_view);
        mSearchView.setOnSearchViewListener(mSearchViewListener);
    }

    @Override
    protected void loadFragment(Fragment fragmentInstance) {
        super.loadFragment(fragmentInstance);
        if (fragmentInstance != null) {
            FragmentManager mFragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            if (fragmentInstance instanceof FragmentUserApp) { // Installed apps
                ft.replace(R.id.home_container, fragmentInstance, AppConstants.FRAGMENT_INSTALLED_APPS_TAG);
            } else if (fragmentInstance instanceof FragmentSystemApp) { // System apps
                ft.replace(R.id.home_container, fragmentInstance, AppConstants.FRAGMENT_SYSTEM_APPS_TAG);
            } else if (fragmentInstance instanceof FragmentFavApp) { // Favorite apps
                ft.replace(R.id.home_container, fragmentInstance, AppConstants.FRAGMENT_FAVORITES_APPS_TAG);
            } else if (fragmentInstance instanceof FragmentDeviceInfo) { // Device Info
                ft.replace(R.id.home_container, fragmentInstance, AppConstants.FRAGMENT_DEVICE_INFO_LAYOUT_TAG);
            } else if (fragmentInstance instanceof FragmentMusicLibrary) { // Music Library
                ft.replace(R.id.home_container, fragmentInstance, AppConstants.FRAGMENT_MUSIC_LIBRARY_LAYOUT_TAG);
            } else if (fragmentInstance instanceof FragmentError) { // Error
                ft.replace(R.id.home_container, fragmentInstance, AppConstants.FRAGMENT_ERROR_LAYOUT_TAG);
            }
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            ft.commit();
        }
    }

    @Override
    protected void getAppsFromDevice() {
        super.getAppsFromDevice();
        showProgressBar(true);
        new GetAllAppsCommand(ActivityNavigationView.this, mOnAllAppsFoundListener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void showProgressBar(boolean shouldShow) {
        super.showProgressBar(shouldShow);
        if (shouldShow) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onBroadcastReceived(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        super.onBroadcastReceived(context, intent);
        switch (intent.getAction()) {
            case AppConstants.ACTION_HIDE_PROGRESS_BAR_CALLBACK:
                showProgressBar(false);
                break;
            case AppConstants.ACTION_FOUND_INSTALLED_APPS_CALLBACK:
                AppUtil.getInstance().setInstalledApps(intent
                        .getParcelableArrayListExtra(AppConstants.EXTRA_KEY_INSTALLED_APPS));
                break;
            case AppConstants.ACTION_FOUND_SYSTEM_APPS_CALLBACK:
                AppUtil.getInstance().setSystemAppsList(intent
                        .getParcelableArrayListExtra(AppConstants.EXTRA_KEY_SYSTEM_APPS));
                break;
            case AppConstants.ACTION_RETRY_AGAIN_CALLBACK:
                getAppsFromDevice();
                break;
            case AppConstants.ACTION_DEFAULT_FRAGMENT_CALLBACK:
                // Installed apps
                setToolbarTitle(baseContext.getString(R.string.action_apps));
                loadFragment(new FragmentUserApp());
                getFavoriteApps();
                break;
            case AppConstants.ACTION_APP_PUT_TO_FAVORITES_CALLBACK:
                getFavoriteApps();
                break;
            case AppConstants.ACTION_APP_REMOVED_FROM_FAVORITES_CALLBACK:
                getFavoriteApps();
                break;
            case AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK:
                hideMenuItem(intent.getBooleanExtra("Menu", false));
                break;
            case AppConstants.ACTION_PULL_TO_REFESH_CALLBACK:
                if (mainPresenter != null) {
                    mainPresenter.getAllApps();
                }
                break;
            case AppConstants.ACTION_MUSIC_LIBRARY_COUNT_CALLBACK:
                if (musicLibraryBadgeView != null) {
                    int musicCount = intent.getIntExtra(AppConstants.EXTRA_KEY_MUSIC_COUNT, 0);
                    musicLibraryBadgeView.setGravity(Gravity.CENTER_VERTICAL);
                    musicLibraryBadgeView.setTypeface(null, Typeface.BOLD);
                    musicLibraryBadgeView.setTextColor(getResources().getColor(R.color.colorAccent));
                    musicLibraryBadgeView.setText(String.valueOf(musicCount));
                }
                break;
        }
    }

    private void applyFont(TextView mToolbarTextView) {
        mToolbarTextView.setTypeface(Typeface.createFromAsset(baseContext.getAssets(),
                AppConstants.FONT_PATH));
    }

    private void checkDeviceRoot() {
        if (AppUtil.getInstance().isRooted()) {
            AppUtil.getInstance()
                    .showSnackbar(drawer, R.string.dialog_root_required_description,
                            BaseTransientBottomBar.LENGTH_LONG);
        }
    }

    private void checkForAppUpdates() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.registerListener(mInstallStateUpdatedListener);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                updateInfo = appUpdateInfo;
                mIsUpdateAvailable.setValue(true);
            } else {
                mIsUpdateAvailable.setValue(false);
            }
        });

        mIsUpdateAvailable.observe(this, aBoolean -> {
            if (aBoolean) {
                showAppUpdateSnackbar(drawer, R.string.update_available,
                        R.string.action_update, false);
            } else {
                AppUtil.getInstance().showSnackbar(drawer, R.string.update_not_available,
                        Snackbar.LENGTH_SHORT);
            }
        });
    }

    private void requestForUpdate(AppUpdateInfo appUpdateInfo)
            throws IntentSender.SendIntentException {
        mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE,
                ActivityNavigationView.this, AppConstants.APP_UPDATE_REQ_CODE);
    }

    /* Displays the snackbar notification and call to action. */
    private void showAppUpdateSnackbar(View view, int resId, int actionResId, boolean isDownloaded) {
        Snackbar.make(view, resId, Snackbar.LENGTH_INDEFINITE).setAction(actionResId, view1 -> {
            if (mIsUpdateAvailable.getValue() != null && !isDownloaded) {
                // For a flexible update, use AppUpdateType.FLEXIBLE
                try {
                    requestForUpdate(updateInfo);
                } catch (IntentSender.SendIntentException e) {
                    PrintLog.getInstance().doPrintErrorLog(LOG_TAG, e.getMessage());
                }
            } else {
                mAppUpdateManager.completeUpdate();
            }
        }).setActionTextColor(getResources().getColor(R.color.colorAccent)).show();
    }

    /**
     * Displays an error layout.
     */
    private void showErrorLayout(AppConstants.ERROR_TYPE errorType) {
        Bundle errorBundle = new Bundle();
        switch (errorType) {
            case NO_DATA:
                errorBundle.putString(AppConstants.EXTRA_KEY_ERROR_TEXT, "No items found");
                errorBundle.putSerializable(AppConstants.EXTRA_KEY_ERROR_TYPE, AppConstants.ERROR_TYPE.NO_DATA);
                break;
            case ERROR:
                errorBundle.putString(AppConstants.EXTRA_KEY_ERROR_TEXT, "Something went wrong");
                errorBundle.putSerializable(AppConstants.EXTRA_KEY_ERROR_TYPE, AppConstants.ERROR_TYPE.ERROR);
                break;
        }
        Fragment errorFragment = FragmentError.getInstance(errorBundle);
        loadFragment(errorFragment);
    }

    private void initializeDrawerCounter(int count, AppConstants.APP_TYPE appType) {
        switch (appType) {
            case INSTALLED:
                if (installedAppsBadgeView != null) {
                    installedAppsBadgeView.setGravity(Gravity.CENTER_VERTICAL);
                    installedAppsBadgeView.setTypeface(null, Typeface.BOLD);
                    installedAppsBadgeView.setTextColor(getResources().getColor(R.color.colorAccent));
                    installedAppsBadgeView.setText(String.valueOf(count));
                }
                break;
            case SYSTEM:
                if (systemAppsBadgeView != null) {
                    systemAppsBadgeView.setGravity(Gravity.CENTER_VERTICAL);
                    systemAppsBadgeView.setTypeface(null, Typeface.BOLD);
                    systemAppsBadgeView.setTextColor(getResources().getColor(R.color.colorAccent));
                    systemAppsBadgeView.setText(String.valueOf(count));
                }
                break;
            case FAVORITES:
                if (favAppsBadgeView != null) {
                    favAppsBadgeView.setGravity(Gravity.CENTER_VERTICAL);
                    favAppsBadgeView.setTypeface(null, Typeface.BOLD);
                    favAppsBadgeView.setTextColor(getResources().getColor(R.color.colorAccent));
                    favAppsBadgeView.setText(String.valueOf(count));
                }
                break;
        }
    }

    private void restoreFlow() {
        // Send appropriate list
        if (AppUtil.getInstance().getInstalledAppsList() != null
                && AppUtil.getInstance().getInstalledAppsList().size() > 0
                && AppUtil.getInstance().getSystemAppsList() != null
                && AppUtil.getInstance().getSystemAppsList().size() > 0) {
            showProgressBar(false);
            mOnAllAppsFoundListener.onInstalledAppsFound(AppUtil.getInstance()
                    .getInstalledAppsList(), AppConstants.APP_TYPE.INSTALLED);
            mOnAllAppsFoundListener.onSystemAppsFound(AppUtil.getInstance()
                    .getSystemAppsList(), AppConstants.APP_TYPE.SYSTEM);
            mOnAllAppsFoundListener.onSystemAppsFound(AppUtil.getInstance()
                    .getFavoriteAppsList(), AppConstants.APP_TYPE.FAVORITES);
            mOnAllAppsFoundListener.onLoadDefaultFragment(true);
            mOnAllAppsFoundListener.onSetAppsCounter(AppUtil.getInstance()
                    .getInstalledAppsList().size(), AppConstants.APP_TYPE.INSTALLED);
            mOnAllAppsFoundListener.onSetAppsCounter(AppUtil.getInstance()
                    .getSystemAppsList().size(), AppConstants.APP_TYPE.SYSTEM);
            mOnAllAppsFoundListener.onSetAppsCounter(AppUtil.getInstance()
                    .getFavoriteAppsList().size(), AppConstants.APP_TYPE.FAVORITES);
        }
        if (AppUtil.getInstance().getMusicInfoList() != null
                && AppUtil.getInstance().getMusicInfoList().size() > 0) {
            mainPresenter.getMusicLibrary();
        }
    }

    @Override
    protected void getFavoriteApps() {
        super.getFavoriteApps();
        List<AppInfo> favAppList = new ArrayList<>();
        // Favorites
        for (AppInfo appInfo : AppUtil.getInstance().getInstalledAppsList()) {
            if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), AppManagerController.getInstance()
                    .getAppPreferences().getFavoriteApps())) {
                favAppList.add(appInfo);
            }
        }
        for (AppInfo appInfo : AppUtil.getInstance().getSystemAppsList()) {
            if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), AppManagerController.getInstance()
                    .getAppPreferences().getFavoriteApps())) {
                favAppList.add(appInfo);
            }
        }

        if (favAppList.isEmpty()) {
            mOnAllAppsFoundListener.onFavAppsFound(favAppList, AppConstants.APP_TYPE.NONE);
        } else {
            mOnAllAppsFoundListener.onFavAppsFound(favAppList, AppConstants.APP_TYPE.FAVORITES);
        }
        mOnAllAppsFoundListener.onSetAppsCounter(favAppList.size(), AppConstants.APP_TYPE.FAVORITES);
    }

    private void hideMenuItem(boolean hide) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (hide) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.home_container);
            if (currentFragment instanceof FragmentFavApp) {
                searchItem.setVisible(false);
            } else if (currentFragment instanceof FragmentDeviceInfo) {
                searchItem.setVisible(false);
            } else if (currentFragment instanceof FragmentMusicLibrary) {
                searchItem.setVisible(false);
            }
        } else {
            searchItem.setVisible(true);
        }
    }

    private OnGetAllAppsListener mOnAllAppsFoundListener = new OnGetAllAppsListener() {
        /**
         * Returns the callback when installed apps are found from the device
         * @param listOfApps the list of apps found
         * @param appType the app type
         */
        @Override
        public void onInstalledAppsFound(List<AppInfo> listOfApps, AppConstants.APP_TYPE appType) {
            Intent i = new Intent();
            switch (appType) {
                case INSTALLED:
                    i.putParcelableArrayListExtra(AppConstants.EXTRA_KEY_INSTALLED_APPS,
                            new ArrayList<AppInfo>(listOfApps));
                    i.setAction(AppConstants.ACTION_FOUND_INSTALLED_APPS_CALLBACK);
                    LocalBroadcastManager.getInstance(ActivityNavigationView.this).sendBroadcast(i);
                    break;
                case NONE:
                    if (listOfApps == null || listOfApps.isEmpty()) {
                        showErrorLayout(AppConstants.ERROR_TYPE.NO_DATA);
                    } else {
                        showErrorLayout(AppConstants.ERROR_TYPE.ERROR);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSystemAppsFound(List<AppInfo> listOfApps, AppConstants.APP_TYPE appType) {
            Intent i = new Intent();
            i.putParcelableArrayListExtra(AppConstants.EXTRA_KEY_SYSTEM_APPS,
                    new ArrayList<AppInfo>(listOfApps));
            i.setAction(AppConstants.ACTION_FOUND_SYSTEM_APPS_CALLBACK);
            LocalBroadcastManager.getInstance(ActivityNavigationView.this).sendBroadcast(i);
        }

        @Override
        public void onFavAppsFound(List<AppInfo> listOfApps, AppConstants.APP_TYPE appType) {
            AppUtil.getInstance().setFavoriteAppsList(listOfApps);
        }

        @Override
        public void onHideProgressBar(boolean isToHide) {
            Intent i = new Intent();
            if (isToHide) {
                i.setAction(AppConstants.ACTION_HIDE_PROGRESS_BAR_CALLBACK);
                LocalBroadcastManager.getInstance(ActivityNavigationView.this).sendBroadcast(i);
            }
        }

        @Override
        public void onLoadDefaultFragment(boolean isToLoadDefault) {
            Intent i = new Intent();
            if (isToLoadDefault) {
                i.setAction(AppConstants.ACTION_DEFAULT_FRAGMENT_CALLBACK);
                LocalBroadcastManager.getInstance(ActivityNavigationView.this).sendBroadcast(i);
            }
        }

        @Override
        public void onSetAppsCounter(int installedAppscount, AppConstants.APP_TYPE appType) {
            initializeDrawerCounter(installedAppscount, appType);
        }
    };

    private final IMainView mMainView = new IMainView() {
        @Override
        public void notifyUiReady() {
            initializeToolbar();
            initializeViews();
            //checkDeviceRoot();

            // Checks for in-app updates
            mainPresenter.checkForAppUpdates();
            AppRater.getInstance().appLaunched(baseContext);
        }

        @Override
        public void checkToRestoreData(Bundle savedInstanceState) {
            if (canRestoreDataFromCache(savedInstanceState)) {
                restoreDataFromCache(savedInstanceState);
                restoreFlow();
            } else {
                mainPresenter.getAllApps();
                mainPresenter.getMusicLibrary();
            }
        }

        @Override
        public void getAllAppsFromDevice() {
            getAppsFromDevice();
        }

        @Override
        public void registerBroadcast() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(AppConstants.ACTION_FOUND_INSTALLED_APPS_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_FOUND_SYSTEM_APPS_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_RETRY_AGAIN_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_HIDE_PROGRESS_BAR_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_DEFAULT_FRAGMENT_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_APP_PUT_TO_FAVORITES_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_APP_REMOVED_FROM_FAVORITES_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_PULL_TO_REFESH_CALLBACK);
            intentFilter.addAction(AppConstants.ACTION_MUSIC_LIBRARY_COUNT_CALLBACK);
            registerBroadcastReceiver(intentFilter);
        }

        @Override
        public void unRegisterBroadcast() {
            unRegisterBroadcastReceiver();
        }

        @Override
        public void onMenuItemClicked(int menuItemId) {
            switch (menuItemId) {
                case R.id.nav_installed_apps:
                    // Installed apps
                    setToolbarTitle(baseContext.getString(R.string.action_apps));
                    loadFragment(new FragmentUserApp());
                    break;
                case R.id.nav_system_apps:
                    setToolbarTitle(baseContext.getString(R.string.action_system_apps));
                    loadFragment(new FragmentSystemApp());
                    break;
                case R.id.nav_favorites:
                    setToolbarTitle(baseContext.getString(R.string.action_favorites));
                    loadFragment(new FragmentFavApp());
                    break;
                case R.id.nav_device_info:
                    setToolbarTitle(baseContext.getString(R.string.action_device));
                    loadFragment(new FragmentDeviceInfo());
                    break;
                case R.id.nav_music_library:
                    setToolbarTitle(baseContext.getString(R.string.action_music_library));
                    loadFragment(new FragmentMusicLibrary());
                    break;
                case R.id.nav_share:
                    AppUtil.getInstance().shareApplication(baseContext);
                    break;
                case R.id.nav_about:
                    startActivity(new Intent(ActivityNavigationView.this, ActivityAboutDev.class));
                    break;
                case R.id.nav_settings:
                    startActivity(new Intent(ActivityNavigationView.this, ActivitySettings.class));
                    break;
            }
        }

        @Override
        public void onBackPressed() {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (doubleBackToExitPressedOnce) {
                    ActivityNavigationView.this.finish();
                }
                doubleBackToExitPressedOnce = true;
                AppUtil.getInstance()
                        .showSnackbar(drawer, R.string.tap_exit, BaseTransientBottomBar.LENGTH_LONG);
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        }

        @Override
        public void setHeaderLayout() {
            if (imageViewHeader != null) {
                int dayOrNight = AppUtil.getInstance().getDayOrNight();
                int drawableResId;
                switch (dayOrNight) {
                    case AppConstants.DAY:
                        drawableResId = R.drawable.header_day;
                        break;
                    case AppConstants.NIGHT:
                        drawableResId = R.drawable.header_night;
                        break;
                    default:
                        drawableResId = R.drawable.header_day;
                        break;
                }
                imageViewHeader.setImageResource(drawableResId);
            }
        }

        @Override
        public void performSearch(String query) {
            // Todo: Add search logic
            Intent search = new Intent();
            search.setAction(AppConstants.ACTION_PERFORM_SEARCH_CALLBACK);
            search.putExtra("Query", query);
            LocalBroadcastManager.getInstance(ActivityNavigationView.this).sendBroadcast(search);
        }

        @Override
        public void checkForUpdates() {
            checkForAppUpdates();
        }

        @Override
        public void getAllMusicFromDevice() {
            new GetAllMusicCommand(baseContext, mMusicLibraryListener)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    private final NavigationView.OnNavigationItemSelectedListener OnNavigationItemSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            switch (id) {
                case R.id.nav_installed_apps:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_system_apps:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_favorites:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_device_info:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_music_library:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_share:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_about:
                    mainPresenter.onItemClicked(id);
                    break;
                case R.id.nav_settings:
                    mainPresenter.onItemClicked(id);
                    break;
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    private final OnSearchViewListener mSearchViewListener = new OnSearchViewListener() {
        @Override
        public void onSearchViewShown() {

        }

        @Override
        public void onSearchViewClosed() {

        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public void onQueryTextChange(String s) {
            mainPresenter.searchClick(s);
        }
    };

    private final InstallStateUpdatedListener mInstallStateUpdatedListener = installState -> {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            showAppUpdateSnackbar(drawer, R.string.restart_to_update, R.string.action_restart, true);
        } else if (installState.installStatus() == InstallStatus.CANCELED) {
            AppUtil.getInstance().showSnackbar(drawer,
                    R.string.app_update_cancelled, Snackbar.LENGTH_LONG);
        } else if (installState.installStatus() == InstallStatus.FAILED) {
            AppUtil.getInstance().showSnackbar(drawer,
                    R.string.app_update_failed, Snackbar.LENGTH_LONG);
        }
    };

    private final IMusicLibraryListener mMusicLibraryListener = musicType -> {
        Intent musicCounterIntent = new Intent(AppConstants.ACTION_MUSIC_LIBRARY_COUNT_CALLBACK);
        switch (musicType) {
            case LIBRARY:
                musicCounterIntent.putExtra(AppConstants.EXTRA_KEY_MUSIC_COUNT,
                        AppUtil.getInstance().getMusicInfoList().size());
                LocalBroadcastManager.getInstance(baseContext).sendBroadcast(musicCounterIntent);
                break;
            case NO_DATA_FOUND:
                musicCounterIntent.putExtra(AppConstants.EXTRA_KEY_MUSIC_COUNT, 0);
                LocalBroadcastManager.getInstance(baseContext).sendBroadcast(musicCounterIntent);
                break;
        }
    };
}