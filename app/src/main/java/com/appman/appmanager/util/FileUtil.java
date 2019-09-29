package com.appman.appmanager.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.data.MusicInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

public class FileUtil {
    private static FileUtil sInstance = null;

    private FileUtil() {}

    public static FileUtil getInstance() {
        if (sInstance == null) {
            sInstance = new FileUtil();
        }
        return sInstance;
    }

    public File[] getDefaultFolderDirectories() {
        return new File[] {
                getDefaultAppFolder(),
                getDefaultCrashLogFolder(),
                getDefaultMusicLibraryFolder()
        };
    }

    /**
     * Default folder where APKs will be saved
     * @return File with the path
     */
    public File getDefaultAppFolder() {
        return new File(Environment.getExternalStorageDirectory() + AppConstants.DEFAULT_APK_FOLDER);
    }

    public File getDefaultCrashLogFolder() {
        return new File(Environment.getExternalStorageDirectory() + AppConstants.DEFAULT_CRASH_LOG_FOLDER);
    }

    public File getDefaultMusicLibraryFolder() {
        return new File(Environment.getExternalStorageDirectory() + AppConstants.DEFAULT_MUSIC_LIBRARY_FOLDER);
    }

    /**
     * Custom folder where APKs will be saved
     * @return File with the path
     */
    private File getAppFolder() {
        AppPreferences appPreferences = AppManagerController.getInstance().getAppPreferences();
        return new File(appPreferences.getCustomPath());
    }

    public Boolean copyFile(AppInfo appInfo) {
        Boolean res = false;

        File initialFile = new File(appInfo.getSource());
        File finalFile = getOutputFilename(appInfo);

        try {
            FileUtils.copyFile(initialFile, finalFile);
            res = true;
        } catch (IOException e) {
            PrintLog.getInstance()
                    .doPrintErrorLog("FileUtil",
                            "Exception while copying file - "
                                    + finalFile + " :: cause - " + e.getMessage());
        }

        return res;
    }

    public Boolean copyMusicFile(MusicInfo musicInfo) {
        Boolean res = false;

        File initialFile = new File(musicInfo.getLocation());
        File finalFile = getMusicOutputFilename(musicInfo);

        try {
            FileUtils.copyFile(initialFile, finalFile);
            res = true;
        } catch (IOException e) {
            PrintLog.getInstance()
                    .doPrintErrorLog("FileUtil",
                            "Exception while copying file - "
                                    + finalFile + " :: cause - " + e.getMessage());
        }
        return res;
    }

    /**
     * Retrieve the name of the extracted APK
     * @param appInfo AppInfo
     * @return String with the output name
     */
    public String getAPKFilename(AppInfo appInfo) {
        AppPreferences appPreferences = AppManagerController.getInstance().getAppPreferences();
        String res;

        switch (appPreferences.getCustomFilename()) {
            case "1":
                res = appInfo.getAPK() + "_" + appInfo.getVersion();
                break;
            case "2":
                res = appInfo.getName() + "_" + appInfo.getVersion();
                break;
            case "4":
                res = appInfo.getName();
                break;
            default:
                res = appInfo.getAPK();
                break;
        }

        return res;
    }

    /**
     * Retrieve the name of the extracted APK with the path
     * @param appInfo AppInfo
     * @return File with the path and output name
     */
    public File getOutputFilename(AppInfo appInfo) {
        return new File(getAppFolder().getPath() + "/" + getAPKFilename(appInfo) + ".apk");
    }

    public File getMusicOutputFilename(MusicInfo musicInfo) {
        AppPreferences appPreferences = AppManagerController.getInstance().getAppPreferences();
        return new File(appPreferences.getMusicLibraryPath() + "/" + musicInfo.getTitle() + ".mp3");
    }

    /**
     * Delete all the extracted APKs
     * @return true if all files have been deleted, false otherwise
     */
    public Boolean deleteAppFiles() {
        Boolean res = false;
        File f = getAppFolder();
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            for (File file : files) {
                file.delete();
            }
            if (f.listFiles().length == 0) {
                res = true;
            }
        }
        return res;
    }

    public boolean isFolderConsistsFiles(File folder) {
        return folder.exists() && folder.isDirectory() && folder.listFiles().length > 0;
    }

    public boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }

    /**
     * Opens Google Play if installed, if not opens browser
     * @param context Context
     * @param id PackageName on Google Play
     */
    public void goToGooglePlay(Context context, String id) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(context.getResources().getString(R.string.app_rate_market_id, id))));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(context.getResources()
                            .getString(R.string.google_play_market_id_alternative, id))));
        }
    }

    /**
     * Opens Google Plus
     * @param context Context
     * @param id Name on Google Play
     */
    public void goToGooglePlus(Context context, String id) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/" + id)));
    }

    /**
     * OPENS FACBOOK ACCOUNT
     * @param context
     * @param id
     */
    /*public static void goToFacebook(Context context, String id){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + id)));
    }*/
    public Intent getFacebookIntent(Context context, String id){
        try{
            context.getPackageManager().getPackageInfo("com.facebook.katana",0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + id));
        }catch (PackageManager.NameNotFoundException nnfe){
            nnfe.getMessage().toString();
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + id));
        }
    }

    /**
     * OPENS TWITTER ACCOUNT
     * @param context
     * @param id
     */
    /*public static void goToTwitter(Context context, String id){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/" + id)));
    }*/

    public Intent getTwitterIntent(Context context, String id){
        try{
            context.getPackageManager().getPackageInfo("com.twitter.android", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id="+id));
        }catch (PackageManager.NameNotFoundException nnfe){
            nnfe.getMessage().toString();
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/" + id));
        }
    }

    /**
     * Retrieve your own app version
     * @param context Context
     * @return String with the app version
     */
    public String getAppVersionName(Context context) {
        String res = "0.0.0.0";
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Retrieve your own app version code
     * @param context Context
     * @return int with the app version code
     */
    public int getAppVersionCode(Context context) {
        int res = 0;
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public Intent getShareIntent(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            Uri apkUri = FileProvider.getUriForFile(AppManagerController.getInstance().getApplicationContext(),
                    AppManagerController.getInstance().getApplicationContext().getPackageName() + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, apkUri);
            intent.setType("application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public Intent getMusicShareIntent(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("audio/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            Uri musicUri = FileProvider.getUriForFile(AppManagerController.getInstance().getApplicationContext(),
                    AppManagerController.getInstance().getApplicationContext().getPackageName() + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, musicUri);
            intent.setType("audio/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    /**
     * Retrieve if an app has been marked as favorite
     * @param apk App to check
     * @param appFavorites Set with apps
     * @return true if the app is marked as favorite, false otherwise
     */
    public Boolean isAppFavorite(String apk, Set<String> appFavorites) {
        Boolean res = false;
        if (appFavorites.contains(apk)) {
            res = true;
        }
        return res;
    }

    /**
     * Save the app as favorite
     * @param context Context
     * @param fab Item of the ActionBar
     * @param isFavorite true if the app is favorite, false otherwise
     */
    public void setAppFavorite(Context context, FloatingActionButton fab, Boolean isFavorite) {
        if (isFavorite) {
            fab.setImageResource(R.drawable.ic_star_white);
        } else {
            fab.setImageResource(R.drawable.ic_star_border_white);
        }
    }

    /**
     * Retrieve if an app is hidden
     * @param appInfo App to check
     * @param appHidden Set with apps
     * @return true if the app is hidden, false otherwise
     */
    public Boolean isAppHidden(AppInfo appInfo, Set<String> appHidden) {
        Boolean res = false;
        if (appHidden.contains(appInfo.toString())) {
            res = true;
        }

        return res;
    }

    /**
     * Save an app icon to cache folder
     * @param context Context
     * @param appInfo App to save icon
     * @return true if the icon has been saved, false otherwise
     */
    public Boolean saveIconToCache(Context context, AppInfo appInfo) {
        Boolean res = false;

        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(appInfo.getAPK(), 0);
            File fileUri = new File(context.getCacheDir(), appInfo.getAPK());
            FileOutputStream out = new FileOutputStream(fileUri);
            Drawable icon = context.getPackageManager().getApplicationIcon(applicationInfo);
            BitmapDrawable iconBitmap = (BitmapDrawable) icon;
            iconBitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
            res = true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Delelete an app icon from cache folder
     * @param context Context
     * @param appInfo App to remove icon
     * @return true if the icon has been removed, false otherwise
     */
    public Boolean removeIconFromCache(Context context, AppInfo appInfo) {
        File file = new File(context.getCacheDir(), appInfo.getAPK());
        return file.delete();
    }

    /**
     * Get an app icon from cache folder
     * @param context Context
     * @param appInfo App to get icon
     * @return Drawable with the app icon
     */
    public Drawable getIconFromCache(Context context, AppInfo appInfo) {
        Drawable res;

        try {
            File fileUri = new File(context.getCacheDir(), appInfo.getAPK());
            Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
            res = new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            res = context.getResources().getDrawable(R.drawable.ic_android);
        }

        return res;
    }
}
