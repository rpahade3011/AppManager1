package com.appman.appmanager.ui.activities.permission;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.ui.activities.base.BaseActivity;
import com.appman.appmanager.ui.activities.main.ActivityNavigationView;
import com.appman.appmanager.util.AppPreferences;
import com.appman.appmanager.util.FileUtil;
import com.appman.appmanager.util.PermissionUtil;
import com.appman.appmanager.util.PrintLog;

import java.io.File;

public class ActivityPermission extends BaseActivity {
    private final String TAG = "ActivityPermission";
    private PermissionPresenter permissionPresenter;
    private AppPreferences appPreferences = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_permission_layout);
        this.appPreferences = AppManagerController.getInstance().getAppPreferences();
        permissionPresenter = new PermissionPresenter(mPermissionView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        initializeViews();
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        findViewById(R.id.buttonGetStarted).setOnClickListener(v -> {
            PrintLog.getInstance().doPrintInfoLog(TAG, "Asking permissions");
            permissionPresenter.askPermissions();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AppPreferences appPreferences = AppManagerController.getInstance().getAppPreferences();
        if (requestCode == PermissionUtil.ALL_PERM_REQ_CODE) {
            if (PermissionUtil.getInstance().verifyResult(grantResults)) {
                PrintLog.getInstance().doPrintInfoLog(TAG, "All permissions has been granted");
                appPreferences.setPermissionPrefs(true);
                permissionPresenter.makeCustomFolders();
                permissionPresenter.navigateToScreen();
            } else {
                permissionPresenter.askPermissions();
            }
        }
    }

    @Override
    protected void navigateToScreen(Intent intentObject) {
        super.navigateToScreen(intentObject);
        PrintLog.getInstance().doPrintInfoLog(TAG, "Navigating to main screen");
        startActivity(intentObject);
        ActivityPermission.this.finish();
    }

    private Intent getActivityIntent() {
        return new Intent(ActivityPermission.this, ActivityNavigationView.class);
    }

    private final IPermissionView mPermissionView = new IPermissionView() {
        @Override
        public void startAskingForPermissions() {
            String[] permissionList = new String[] {
                    PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION,
                    PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION
            };
            ActivityCompat.requestPermissions(ActivityPermission.this,
                    permissionList, PermissionUtil.ALL_PERM_REQ_CODE);
        }

        @Override
        public void navigateToMainScreen() {
            navigateToScreen(getActivityIntent());
        }

        @Override
        public void createCustomFolders() {
            // Setting APK folder
            boolean isFolderCreated = false;
            File[] listOfDir = FileUtil.getInstance().getDefaultFolderDirectories();
            for (File dir : listOfDir) {
                if (!dir.exists()) {
                    isFolderCreated = dir.mkdirs();
                    if (isFolderCreated) {
                        PrintLog.getInstance().doPrintInfoLog(TAG, dir.getName()
                                + " is created with " + dir.getAbsolutePath() + " path");
                    }
                } else {
                    PrintLog.getInstance().doPrintInfoLog(TAG, dir.getName()
                            + " is already created at " + dir.getAbsolutePath() + " path");
                }
            }
            appPreferences.setCustomPath(FileUtil.getInstance().getDefaultAppFolder().getPath());
            appPreferences.setCrashLogPath(FileUtil.getInstance().getDefaultCrashLogFolder().getPath());
            appPreferences.setMusicLibraryPath(FileUtil.getInstance().getDefaultMusicLibraryFolder().getPath());
        }
    };
}

