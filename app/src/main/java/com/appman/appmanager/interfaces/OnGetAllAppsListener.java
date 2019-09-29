package com.appman.appmanager.interfaces;

import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;

import java.util.List;

public interface OnGetAllAppsListener {
    void onInstalledAppsFound(List<AppInfo> listOfApps, AppConstants.APP_TYPE appType);
    void onSystemAppsFound(List<AppInfo> listOfApps, AppConstants.APP_TYPE appType);
    void onFavAppsFound(List<AppInfo> listOfApps, AppConstants.APP_TYPE appType);
    void onHideProgressBar(boolean isToHide);
    void onLoadDefaultFragment(boolean isToLoadDefault);
    void onSetAppsCounter(int count, AppConstants.APP_TYPE appType);
}
