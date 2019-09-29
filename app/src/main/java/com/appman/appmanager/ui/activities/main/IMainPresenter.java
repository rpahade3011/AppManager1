package com.appman.appmanager.ui.activities.main;

import android.os.Bundle;

public interface IMainPresenter {
    void notifyUiReady();
    void canRestoreData(Bundle savedInstance);
    void getAllApps();
    void registerBroadcastReceiver();
    void unRegisterBroadcastReceiver();
    void onItemClicked(int itemId);
    void onBackPressed();
    void setHeader();
    void searchClick(String s);
    void checkForAppUpdates();
    void getMusicLibrary();
}
