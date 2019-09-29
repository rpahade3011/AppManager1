package com.appman.appmanager.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.data.MusicInfo;
import com.appman.appmanager.interfaces.IAlertDialogCallback;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppUtil {
    private static AppUtil sInstance = null;
    private List<AppInfo> installedAppsList;
    private List<AppInfo> systemAppsList;
    private List<AppInfo> favoriteAppsList;

    private List<MusicInfo> musicInfoList;

    private AppUtil() { }

    public static AppUtil getInstance() {
        if (sInstance == null) {
            sInstance = new AppUtil();
        }
        return sInstance;
    }

    public void setInstalledApps(List<AppInfo> list) {
        this.installedAppsList = list;
    }

    public void setSystemAppsList(List<AppInfo> list) {
        this.systemAppsList = list;
    }

    public List<AppInfo> getInstalledAppsList() {
        return installedAppsList;
    }

    public List<AppInfo> getSystemAppsList() {
        return systemAppsList;
    }

    public List<AppInfo> getFavoriteAppsList() {
        return favoriteAppsList;
    }

    public void setFavoriteAppsList(List<AppInfo> favoriteAppsList) {
        this.favoriteAppsList = favoriteAppsList;
    }

    public List<MusicInfo> getMusicInfoList() {
        return musicInfoList;
    }

    public void setMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList = musicInfoList;
    }

    public Intent createAppInfoIntent(String app_name, String app_apk,
                                      String app_version, String app_source,
                                      String app_data, String apk_size, Bitmap app_icon,
                                      Boolean app_isSystem, Activity activity, Class<?> cls) {
        Intent i = new Intent(activity, cls);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_NAME, app_name);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_APK, app_apk);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_VERSION, app_version);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_SOURCE, app_source);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_DATA, app_data);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_SIZE, apk_size);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_ICON, app_icon);
        i.putExtra(AppConstants.INTENT_EXTRA_NAME_APP_SYSTEM, app_isSystem);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    public void showSnackbar(View view, int resId, int duration) {
        Snackbar.make(view, resId, duration).show();
    }

    public void makeToast(Context context, String message) {
        Toast t = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();
    }

    public void showAlert(String title, String msg, IAlertDialogCallback alertDialogCallback) {
        AlertDialog.Builder alertBuilder =
                new AlertDialog.Builder(AppManagerController.getInstance().getCurrentActivity());
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(msg);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
            if (alertDialogCallback != null) {
                alertDialogCallback.onOkClicked();
                dialogInterface.dismiss();
            }
        });
        alertBuilder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
            if (alertDialogCallback != null) {
                alertDialogCallback.onCancelClicked();
                dialogInterface.dismiss();
            }
        });
        alertBuilder.show();
    }

    public int getDayOrNight() {
        int actualHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (actualHour >= 8 && actualHour < 19) {
            return AppConstants.DAY;
        } else {
            return AppConstants.NIGHT;
        }
    }

    public boolean isActivityResolvable(Intent intent, Context ctx) {
        return intent.resolveActivity(ctx.getPackageManager()) != null;
    }

    public void shareApplication(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setType(AppConstants.INTENT_EXTRA_SHARE_APP_TYPE);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.app_share_text,
                context.getResources().getString(R.string.app_google_play_url_link)));
        context.startActivity(Intent.createChooser(shareIntent,
                AppConstants.INTENT_EXTRA_SHARE_APP_CHOOSER_TEXT));
    }

    public String getAdMobDeviceId(Activity activity) {
        String androidId = getDeviceId(activity);
        return getMessageDigest(androidId).toUpperCase();
    }

    private String getDeviceId(Activity activity) {
        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return deviceId;
    }

    private String getMessageDigest(final String android_id) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(android_id.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e("AppUtil", "Exception - " + e.getMessage());
        }
        return "";
    }

    public String convertToDate(long date) {
        String new_date = "";
        try {
            Date date1 = new Date(date);
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM", Locale.getDefault());
            new_date = sdf.format(date1);

        } catch (Exception e) {
            PrintLog.getInstance().doPrintErrorLog("AppUtil",
                    "Exception while converting date" + " : " + e.getLocalizedMessage());
        }
        return new_date;
    }

    public String getDate(long time) {
        String dateFormat = "EEE, d MMMM yyyy";

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return formatter.format(calendar.getTime());
    }

    public Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public boolean isRooted() {
        int rootStatus = AppManagerController.getInstance().getAppPreferences().getRootStatus();
        boolean isRooted = false;
        if (rootStatus == AppConstants.ROOT_STATUS_NOT_CHECKED) {
            isRooted = isRootByBuildTag() || isRootedByFileSU() || isRootedByExecutingCommand();
            AppManagerController.getInstance().getAppPreferences().setRootStatus(AppConstants.ROOT_STATUS_ROOTED);
        } else if (rootStatus == AppConstants.ROOT_STATUS_ROOTED) {
            isRooted = true;
        }
        return isRooted;
    }

    private boolean isRootByBuildTag() {
        String buildTags = Build.TAGS;
        return ((buildTags != null && buildTags.contains("test-keys")));
    }

    private boolean isRootedByFileSU() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private boolean isRootedByExecutingCommand() {
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su")
                || canExecuteCommand("which su");
    }

    private boolean canExecuteCommand(String command) {
        boolean isExecuted;
        try {
            Runtime.getRuntime().exec(command);
            isExecuted = true;
        } catch (Exception e) {
            isExecuted = false;
        }

        return isExecuted;
    }

    public boolean removeWithRootPermission(String directory) {
        boolean status = false;
        try {
            String[] command = new String[]{"su", "-c", "rm -rf " + directory};
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            int i = process.exitValue();
            if (i == 0) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    public boolean uninstallWithRootPermission(String source) {
        boolean status = false;
        try {
            String[] command_write = new String[]{"su", "-c", "mount -o rw,remount /system\n"};
            String[] command_delete = new String[]{"su", "-c", "rm -r " + "/" + source + "\n"};
            String[] command_read = new String[]{"su", "-c", "mount -o ro,remount /system\n"};

            Process process = Runtime.getRuntime().exec(command_write);
            process.waitFor();
            int i = process.exitValue();
            if (i == 0) {
                process = Runtime.getRuntime().exec(command_delete);
                process.waitFor();
                i = process.exitValue();
                if (i == 0) {
                    process = Runtime.getRuntime().exec(command_read);
                    process.waitFor();
                    i = process.exitValue();
                    if (i == 0) {
                        status = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    public boolean rebootSystem() {
        boolean status = false;
        try {
            String [] command = new String[]{"su", "-c", "reboot\n"};

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            int i = process.exitValue();
            if (i == 0) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private boolean clearApplicationData(Context ctx) {
        return  ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
    }
}
