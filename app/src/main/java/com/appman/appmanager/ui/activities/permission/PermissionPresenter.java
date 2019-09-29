package com.appman.appmanager.ui.activities.permission;

public class PermissionPresenter implements IPermissionPresenter {
    private IPermissionView permissionView;

    public PermissionPresenter (IPermissionView view) {
        this.permissionView = view;
    }

    @Override
    public void askPermissions() {
        permissionView.startAskingForPermissions();
    }

    @Override
    public void navigateToScreen() {
        permissionView.navigateToMainScreen();
    }

    @Override
    public void makeCustomFolders() {
        permissionView.createCustomFolders();
    }
}
