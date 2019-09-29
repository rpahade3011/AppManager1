package com.appman.appmanager.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Button;

import com.appman.appmanager.R;

public class AppRater {
    private static AppRater sInstance = null;
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    private AppRater() {}

    public static AppRater getInstance() {
        if (sInstance == null) {
            sInstance = new AppRater();
        }
        return sInstance;
    }

    /**
     * Method to initialize and setup the shared prefs values,
     * get the system date and installed date of application, wait for N days
     * the call the {@link AppRater#showRateDialog}
     * @param context
     */
    public void appLaunched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("appmanager_apprater", 0);

        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(context, editor);
            }
        }

        editor.apply();
    }

    /**
     * Method to display the custom dialog to engaged users to rate the application
     * @param mContext
     * @param editor
     */
    private void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        String APP_TITLE = mContext.getResources().getString(R.string.app_name);
        String APP_PNAME = mContext.getPackageName();
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.apprater_activity);

        final Button btnRateMe = dialog.findViewById(R.id.buttonRateApp);
        final Button btnRemindLater = dialog.findViewById(R.id.buttonRemindLater);
        final Button btnNoThanks = dialog.findViewById(R.id.buttonNoThanks);

        // Button Rate Clicked
        btnRateMe.setOnClickListener(v -> {
            try{
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mContext.getString(R.string.app_rate_market_id, APP_PNAME))));
            } catch (Exception e){
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mContext.getString(R.string.app_rate_market_id_alternative, APP_PNAME))));
            }

            dialog.dismiss();
        });

        // Button Remind Later Clicked
        btnRemindLater.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Button No Thanks Clicked
        btnNoThanks.setOnClickListener(v -> {
            if (editor != null) {
                editor.putBoolean("dontshowagain", true);
                editor.apply();
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
