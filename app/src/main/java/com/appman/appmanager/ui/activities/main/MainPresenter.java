package com.appman.appmanager.ui.activities.main;

import android.os.Bundle;

public class MainPresenter implements IMainPresenter {
    private IMainView mainView;

    public MainPresenter(IMainView view) {
        this.mainView = view;
    }

    @Override
    public void notifyUiReady() {
        mainView.notifyUiReady();
    }

    @Override
    public void canRestoreData(Bundle savedInstance) {
        mainView.checkToRestoreData(savedInstance);
    }

    @Override
    public void getAllApps() {
        mainView.getAllAppsFromDevice();
    }

    @Override
    public void registerBroadcastReceiver() {
        mainView.registerBroadcast();
    }

    @Override
    public void unRegisterBroadcastReceiver() {
        mainView.unRegisterBroadcast();
    }

    @Override
    public void onItemClicked(int itemId) {
        mainView.onMenuItemClicked(itemId);
    }

    @Override
    public void onBackPressed() {
        mainView.onBackPressed();
    }

    @Override
    public void setHeader() {
        mainView.setHeaderLayout();
    }

    @Override
    public void searchClick(String s) {
        mainView.performSearch(s);
    }

    @Override
    public void checkForAppUpdates() { mainView.checkForUpdates(); }

    @Override
    public void getMusicLibrary() {
        mainView.getAllMusicFromDevice();
    }
}
