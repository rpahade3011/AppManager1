package com.appman.appmanager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.appman.appmanager.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rudhraksh.pahade on 03-12-2015.
 */
public class AppPreferences {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public static final String KeyPrimaryColor = "prefPrimaryColor";
    public static final String KeyFABColor = "prefFABColor";
    public static final String KeyFABShow = "prefFABShow";
    public static final String KeyNavigationBlack = "prefNavigationBlack";
    public static final String KeyCustomFilename = "prefCustomFilename";
    public static final String KeySortMode = "prefSortMode";
    public static final String KeyIsRooted = "prefIsRooted";
    public static final String KeyCustomPath = "prefCustomPath";
    public static final String KeyCrashLogPath = "prefCrashLogPath";
    public static final String KeyMusicLibraryPath = "prefMusicLibraryPath";

    // List
    public static final String KeyFavoriteApps = "prefFavoriteApps";
    public static final String KeyHiddenApps = "prefHiddenApps";

    // Permissions
    private static final String KeyPermissionsEnabled = "prefPermissionsEnabled";

    public AppPreferences(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
        this.context = context;
    }

    public int getRootStatus() {
        return sharedPreferences.getInt(KeyIsRooted, 0);
    }

    public void setRootStatus(int rootStatus) {
        editor.putInt(KeyIsRooted, rootStatus);
        editor.apply();
    }

    public int getPrimaryColorPref() {
        return sharedPreferences.getInt(KeyPrimaryColor, context.getResources().getColor(R.color.primary));
    }
    public void setPrimaryColorPref(Integer res) {
        editor.putInt(KeyPrimaryColor, res);
        editor.apply();
    }

    public int getFABColorPref() {
        return sharedPreferences.getInt(KeyFABColor, context.getResources().getColor(R.color.fab));
    }
    public void setFABColorPref(Integer res) {
        editor.putInt(KeyFABColor, res);
        editor.apply();
    }

    public Boolean getNavigationBlackPref() {
        return sharedPreferences.getBoolean(KeyNavigationBlack, false);
    }
    public void setNavigationBlackPref(Boolean res) {
        editor.putBoolean(KeyNavigationBlack, res);
        editor.apply();
    }

    public Boolean getFABShowPref() {
        return sharedPreferences.getBoolean(KeyFABShow, false);
    }
    public void setFABShowPref(Boolean res) {
        editor.putBoolean(KeyFABShow, res);
        editor.apply();
    }

    public String getCustomFilename() {
        return sharedPreferences.getString(KeyCustomFilename, "1");
    }
    public void setCustomFilename(String res) {
        editor.putString(KeyCustomFilename, res);
        editor.apply();
    }

    public String getSortMode() {
        return sharedPreferences.getString(KeySortMode, "1");
    }
    public void setSortMode(String res) {
        editor.putString(KeySortMode, res);
        editor.apply();
    }

    public String getCustomPath() {
        return sharedPreferences.getString(KeyCustomPath,
                FileUtil.getInstance().getDefaultAppFolder().getPath());
    }

    public void setCustomPath(String path) {
        editor.putString(KeyCustomPath, path);
        editor.apply();
    }

    public String getCrashLogPath() {
        return sharedPreferences.getString(KeyCrashLogPath,
                FileUtil.getInstance().getDefaultCrashLogFolder().getPath());
    }

    public void setCrashLogPath(String path) {
        editor.putString(KeyCrashLogPath, path);
        editor.apply();
    }

    public void setMusicLibraryPath(String path) {
        editor.putString(KeyMusicLibraryPath, path);
        editor.apply();
    }

    public String getMusicLibraryPath() {
        return sharedPreferences.getString(KeyMusicLibraryPath,
                FileUtil.getInstance().getDefaultMusicLibraryFolder().getPath());
    }

    public Set<String> getFavoriteApps() {
        return sharedPreferences.getStringSet(KeyFavoriteApps, new HashSet<String>());
    }

    public void setFavoriteApps(Set<String> favoriteApps) {
        editor.remove(KeyFavoriteApps);
        editor.commit();
        editor.putStringSet(KeyFavoriteApps, favoriteApps);
        editor.apply();
    }

    public Set<String> getHiddenApps() {
        return sharedPreferences.getStringSet(KeyHiddenApps, new HashSet<String>());
    }

    public void setHiddenApps(Set<String> hiddenApps) {
        editor.remove(KeyHiddenApps);
        editor.commit();
        editor.putStringSet(KeyHiddenApps, hiddenApps);
        editor.apply();
    }

    public void setPermissionPrefs(boolean isEnabled) {
        editor.putBoolean(KeyPermissionsEnabled, isEnabled);
        editor.apply();
    }

    public boolean getPermissionPrefs() {
        return sharedPreferences.getBoolean(KeyPermissionsEnabled, false);
    }
}
