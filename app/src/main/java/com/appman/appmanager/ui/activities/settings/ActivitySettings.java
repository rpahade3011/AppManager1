package com.appman.appmanager.ui.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.interfaces.IAlertDialogCallback;
import com.appman.appmanager.ui.activities.base.BaseActivity;
import com.appman.appmanager.util.AppPreferences;
import com.appman.appmanager.util.AppUtil;
import com.appman.appmanager.util.FileUtil;
import com.appman.appmanager.util.PrintLog;

public class ActivitySettings extends BaseActivity {
    private final String TAG = "ActivitySettings";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_app_settings);
        initializeToolbar();
        if (savedInstanceState == null) {
            PrintLog.getInstance().doPrintInfoLog(TAG, "creating preferences");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.app_settings_wrapper_layout, new FragmentAppSettings()).commit();
        }
    }

    @Override
    protected void initializeToolbar() {
        super.initializeToolbar();
        Toolbar settingsToolbar = findViewById(R.id.app_settings_toolbar);
        setSupportActionBar(settingsToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.action_settings));
        }
        if (settingsToolbar != null) {
            settingsToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            settingsToolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));
            settingsToolbar.setNavigationOnClickListener(view -> onBackPressed());
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        setToolbarTypeface(settingsToolbar);
    }

    @Override
    protected void setToolbarTypeface(Toolbar toolbar) {
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

    private void applyFont(TextView mToolbarTextView) {
        mToolbarTextView.setTypeface(Typeface.createFromAsset(ActivitySettings.this.getAssets(),
                AppConstants.FONT_PATH));
    }

    public static class FragmentAppSettings extends PreferenceFragmentCompat {
        private static final String TAG = "FragmentAppSettings";
        private Context mContext = null;

        private AppPreferences appPreferences = null;

        private Preference prefVersion, prefDeleteAll, prefDefaultPath,
                prefDefaultLogsPath, prefDefaultMusicPath, prefPrivacyPolicy;

        private ListPreference prefCustomFilename, prefSortMode;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.app_settings_preferences);
            this.mContext = getActivity().getApplicationContext();
            this.appPreferences = AppManagerController.getInstance().getAppPreferences();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangedCallback);

            initializeViews();

            // prefCustomFilename
            setCustomFilenameSummary();

            // prefSortMode
            setSortModeSummary();

            // set all default folders
            setAllDefaultFoldersSummary();

            String versionName = FileUtil.getInstance().getAppVersionName(mContext);
            int versionCode = FileUtil.getInstance().getAppVersionCode(mContext);
            prefVersion.setTitle(getResources().getString(R.string.app_name)
                    + " v" + versionName + " (" + versionCode + ")");

            // prefDeleteAll
            prefDeleteAll.setOnPreferenceClickListener(preference -> {
                prefDeleteAll.setEnabled(false);
                if (FileUtil.getInstance().isFolderConsistsFiles(FileUtil.getInstance().getDefaultAppFolder())) {
                    AppUtil.getInstance().showAlert("Confirm Delete",
                            "Are you sure you want to delete all the files stored in the extracted folder?",
                            new IAlertDialogCallback() {
                                @Override
                                public void onOkClicked() {
                                    prefDeleteAll.setSummary(R.string.deleting);
                                    deleteAppFiles();
                                }

                                @Override
                                public void onCancelClicked() {

                                }
                            });
                } else {
                    AppUtil.getInstance().makeToast(mContext,
                            "There are no APKs to be deleted. Try extracting some!");
                }
                prefDeleteAll.setEnabled(true);
                return true;
            });

            // Privacy Policy
            prefPrivacyPolicy.setOnPreferenceClickListener(preference -> {
                // Opening a url
                Intent privacyPolicyIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mContext.getResources().getString(R.string.appmanager_privacy_policy_url)));
                startActivity(privacyPolicyIntent);
                return true;
            });
        }

        private void initializeViews() {
            prefVersion = findPreference("prefVersion");
            prefDeleteAll = findPreference("prefDeleteAll");
            prefCustomFilename = (ListPreference) findPreference("prefCustomFilename");
            prefSortMode = (ListPreference) findPreference("prefSortMode");
            prefDefaultPath = findPreference("prefDefaultPath");
            prefDefaultLogsPath = findPreference("prefDefaultLogsPath");
            prefDefaultMusicPath = findPreference("prefDefaultMusicPath");
            prefPrivacyPolicy = findPreference("prefPrivacyPolicy");
        }

        private void setCustomFilenameSummary() {
            int filenameValue = Integer.valueOf(appPreferences.getCustomFilename()) - 1;
            prefCustomFilename.setSummary(mContext.getResources().getStringArray(R.array.filenameEntries)[filenameValue]);
        }

        private void setSortModeSummary() {
            int sortValue = Integer.valueOf(appPreferences.getSortMode()) - 1;
            prefSortMode.setSummary(mContext.getResources().getStringArray(R.array.sortEntries)[sortValue]);
        }

        private void setAllDefaultFoldersSummary() {
            String defaultAPKPath = appPreferences.getCustomPath();
            String defaultCrashLogsPath = appPreferences.getCrashLogPath();
            String defaultMusicLibraryPath = appPreferences.getMusicLibraryPath();

            if (defaultAPKPath.equals(FileUtil.getInstance().getDefaultAppFolder().getPath())) {
                prefDefaultPath.setSummary(mContext.getResources().getString(R.string.button_default)
                        + ": " + FileUtil.getInstance().getDefaultAppFolder().getPath());
            } else {
                prefDefaultPath.setSummary(defaultAPKPath);
            }

            if (defaultCrashLogsPath.equals(FileUtil.getInstance().getDefaultCrashLogFolder().getPath())) {
                prefDefaultLogsPath.setSummary(mContext.getResources().getString(R.string.button_default)
                        + ": " + FileUtil.getInstance().getDefaultCrashLogFolder().getPath());
            } else {
                prefDefaultLogsPath.setSummary(defaultCrashLogsPath);
            }

            if (defaultMusicLibraryPath.equals(FileUtil.getInstance().getDefaultMusicLibraryFolder().getPath())) {
                prefDefaultMusicPath.setSummary(mContext.getResources().getString(R.string.button_default)
                        + ": " + FileUtil.getInstance().getDefaultMusicLibraryFolder().getPath());
            } else {
                prefDefaultMusicPath.setSummary(defaultMusicLibraryPath);
            }
        }

        private void deleteAppFiles() {
            boolean deleteAll = FileUtil.getInstance().deleteAppFiles();
            if (deleteAll) {
                prefDeleteAll.setSummary(R.string.deleting_done);
            } else {
                prefDeleteAll.setSummary(R.string.deleting_error);
            }
        }

        private final SharedPreferences.OnSharedPreferenceChangeListener
                mOnSharedPreferenceChangedCallback = (sharedPreferences, s) -> {
            Preference pref = findPreference(s);
            if (pref == prefCustomFilename) {
                setCustomFilenameSummary();
            } else if (pref == prefSortMode) {
                setSortModeSummary();
            }
        };
    }
}
