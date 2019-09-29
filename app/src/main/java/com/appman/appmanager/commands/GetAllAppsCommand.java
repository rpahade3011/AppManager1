package com.appman.appmanager.commands;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.format.Formatter;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.interfaces.OnGetAllAppsListener;
import com.appman.appmanager.ui.activities.main.ActivityNavigationView;
import com.appman.appmanager.util.AppPreferences;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GetAllAppsCommand extends AsyncTask<Void, String, Void> {

    private Integer totalApps;
    private Integer actualApps;
    private WeakReference<ActivityNavigationView> mainActivityReference;
    @SuppressLint("StaticFieldLeak")
    private ActivityNavigationView mainActivity;
    private List<AppInfo> appList;
    private List<AppInfo> appSystemList;
    private AppPreferences appPreferences;
    private OnGetAllAppsListener mInstalledAppsListener;


    public GetAllAppsCommand(ActivityNavigationView activity, OnGetAllAppsListener listener) {
        this.mainActivityReference = new WeakReference<>(activity);
        this.mainActivity = mainActivityReference.get();
        this.actualApps = 0;
        this.appList = new ArrayList<>();
        this.appSystemList = new ArrayList<>();
        this.appPreferences = AppManagerController.getInstance().getAppPreferences();
        this.mInstalledAppsListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final PackageManager packageManager = mainActivity.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        Set<String> hiddenApps = appPreferences.getHiddenApps();
        totalApps = packages.size() + hiddenApps.size();
        // Get Sort Mode
        switch (appPreferences.getSortMode()) {
            default:
                // Comparator by Name (default)
                Collections.sort(packages, (p1, p2) ->
                        packageManager.getApplicationLabel(p1.applicationInfo)
                        .toString().toLowerCase()
                        .compareTo(packageManager
                                .getApplicationLabel(p2.applicationInfo)
                                .toString().toLowerCase()));
                break;
            case "2":
                // Comparator by Size
                Collections.sort(packages, (p1, p2) -> {
                    Long size1 = new File(p1.applicationInfo.sourceDir).length();
                    Long size2 = new File(p2.applicationInfo.sourceDir).length();
                    return size2.compareTo(size1);
                });
                break;
            case "3":
                // Comparator by Installation Date (default)
                Collections.sort(packages, (p1, p2) -> Long.toString(p2.firstInstallTime)
                        .compareTo(Long.toString(p1.firstInstallTime)));
                break;
            case "4":
                // Comparator by Last Update
                Collections.sort(packages, (p1, p2) -> Long.toString(p2.lastUpdateTime)
                        .compareTo(Long.toString(p1.lastUpdateTime)));
                break;
        }
        // Installed & System Apps
        for (PackageInfo packageInfo : packages) {
            if (!(packageManager.getApplicationLabel(packageInfo.applicationInfo)
                    .equals("") || packageInfo.packageName.equals(""))) {
                if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                    try {
                        // Non System Apps
                        String apkTextSize = Formatter.formatFileSize(mainActivity.getApplicationContext(),
                                new File(packageInfo.applicationInfo.sourceDir).length());
                        AppInfo tempApp = new AppInfo(packageManager
                                .getApplicationLabel(packageInfo.applicationInfo)
                                .toString(), packageInfo.packageName,
                                packageInfo.versionName,
                                packageInfo.applicationInfo.sourceDir,
                                packageInfo.applicationInfo.dataDir, apkTextSize,
                                packageManager.getApplicationIcon(packageInfo.applicationInfo),
                        false);
                        appList.add(tempApp);
                    } catch (OutOfMemoryError e) {
                        //TODO Workaround to avoid FC on some devices (OutOfMemoryError). Drawable should be cached before.
                        String apkTextSize = Formatter.formatFileSize(mainActivity.getApplicationContext(),
                                new File(packageInfo.applicationInfo.sourceDir).length());
                        AppInfo tempApp = new AppInfo(packageManager
                                .getApplicationLabel(packageInfo.applicationInfo)
                                .toString(), packageInfo.packageName,
                                packageInfo.versionName, packageInfo.applicationInfo.sourceDir,
                                packageInfo.applicationInfo.dataDir, apkTextSize,
                                mainActivity.getDrawable(R.drawable.ic_android),
                                false);
                        appList.add(tempApp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        // System Apps
                        String apkTextSize = Formatter.formatFileSize(mainActivity.getApplicationContext(),
                                new File(packageInfo.applicationInfo.sourceDir).length());
                        AppInfo tempApp = new AppInfo(packageManager
                                .getApplicationLabel(packageInfo.applicationInfo)
                                .toString(), packageInfo.packageName,
                                packageInfo.versionName, packageInfo.applicationInfo.sourceDir,
                                packageInfo.applicationInfo.dataDir, apkTextSize,
                                packageManager.getApplicationIcon(packageInfo.applicationInfo),
                                true);
                        appSystemList.add(tempApp);
                    } catch (OutOfMemoryError e) {
                        //TODO Workaround to avoid FC on some devices (OutOfMemoryError). Drawable should be cached before.
                        String apkTextSize = Formatter.formatFileSize(mainActivity.getApplicationContext(),
                                new File(packageInfo.applicationInfo.sourceDir).length());
                        AppInfo tempApp = new AppInfo(packageManager
                                .getApplicationLabel(packageInfo.applicationInfo)
                                .toString(), packageInfo.packageName,
                                packageInfo.versionName, packageInfo.applicationInfo.sourceDir,
                                packageInfo.applicationInfo.dataDir, apkTextSize,
                                mainActivity.getDrawable(R.drawable.ic_android),
                                false);
                        appSystemList.add(tempApp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            actualApps++;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // Check for list is not empty
        if (appList.isEmpty() && appSystemList.isEmpty()) {
            mInstalledAppsListener.onInstalledAppsFound(appList, AppConstants.APP_TYPE.NONE);
            mInstalledAppsListener.onSystemAppsFound(appSystemList, AppConstants.APP_TYPE.NONE);
        } else if (appList.size() == 0) {
            mInstalledAppsListener.onInstalledAppsFound(appList, AppConstants.APP_TYPE.NONE);
        } else if (appSystemList.size() == 0) {
            mInstalledAppsListener.onSystemAppsFound(appSystemList, AppConstants.APP_TYPE.NONE);
        }

        // Send appropriate list
        if (appList != null
                && appList.size() > 0
                && appSystemList != null
                && appSystemList.size() > 0) {
            mInstalledAppsListener.onHideProgressBar(true);
            mInstalledAppsListener.onInstalledAppsFound(appList, AppConstants.APP_TYPE.INSTALLED);
            mInstalledAppsListener.onSystemAppsFound(appSystemList, AppConstants.APP_TYPE.SYSTEM);
            mInstalledAppsListener.onLoadDefaultFragment(true);
            mInstalledAppsListener.onSetAppsCounter(appList.size(), AppConstants.APP_TYPE.INSTALLED);
            mInstalledAppsListener.onSetAppsCounter(appSystemList.size(), AppConstants.APP_TYPE.SYSTEM);
        }
    }
}
