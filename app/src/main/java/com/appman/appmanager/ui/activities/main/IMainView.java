package com.appman.appmanager.ui.activities.main;

import android.os.Bundle;

public interface IMainView {
    void notifyUiReady();
    void checkToRestoreData(Bundle savedInstanceState);
    void getAllAppsFromDevice();
    void registerBroadcast();
    void unRegisterBroadcast();
    void onMenuItemClicked(int menuItemId);
    void onBackPressed();
    void setHeaderLayout();
    void performSearch(String query);
    void checkForUpdates();
    void getAllMusicFromDevice();
}
