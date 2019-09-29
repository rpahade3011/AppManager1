package com.appman.appmanager.ui.activities.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.ui.activities.base.BaseActivity;
import com.appman.appmanager.ui.activities.main.ActivityNavigationView;
import com.appman.appmanager.ui.activities.permission.ActivityPermission;
import com.appman.appmanager.util.AppPreferences;
import com.appman.appmanager.util.PrintLog;

public class SplashActivity extends BaseActivity {

    private static final String LOG_TAG = "SplashActivity";
    private long SPLASH_SCREEN_MILLIS = 3000;
    private long SPLASH_SCREEN_MILLIS_INTERVAL = 1000;

    private Animation mainAnimation;
    private Animation appImageAnimation;

    private TextView tvSplashAppName;
    private ImageView imgVwAppIcon;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        initializeViews();
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        relativeLayout = findViewById(R.id.rel_lay);
        imgVwAppIcon = findViewById(R.id.imgVwAppIcon);
        tvSplashAppName = findViewById(R.id.tvSplashAppName);
        mainAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_main_animation_alpha);

        mainAnimation.reset();
        relativeLayout.clearAnimation();
        relativeLayout.startAnimation(mainAnimation);

        appImageAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_app_icon_translate);
        appImageAnimation.reset();
        imgVwAppIcon.clearAnimation();
        imgVwAppIcon.setAnimation(appImageAnimation);

        continueSplashScreen();
    }

    @Override
    protected void navigateToScreen(Intent intentObject) {
        super.navigateToScreen(intentObject);
        startActivity(intentObject);
        SplashActivity.this.finish();
    }

    private void continueSplashScreen() {
        CountDownTimer splashScreenTimer = new CountDownTimer(SPLASH_SCREEN_MILLIS,
                SPLASH_SCREEN_MILLIS_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            @Override
            public void onFinish() {
                if (tvSplashAppName.getVisibility() == View.GONE) {
                    tvSplashAppName.setVisibility(View.VISIBLE);

                    mainAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.slide_in_right);
                    mainAnimation.reset();
                    tvSplashAppName.clearAnimation();
                    tvSplashAppName.setAnimation(mainAnimation);

                    new Handler().postDelayed(() -> {
                        PrintLog.getInstance().doPrintInfoLog(LOG_TAG, "Exiting splash");
                        exitSplash();
                    }, 500);
                }
            }
        };
        splashScreenTimer.start();
    }

    private void exitSplash() {
        AppPreferences appPreferences = AppManagerController.getInstance().getAppPreferences();
        if (appPreferences != null) {
            if (!appPreferences.getPermissionPrefs()) {
                navigateToScreen(getPermissionIntent());
            } else {
                navigateToMainScreen(getMainIntent());
            }
        }
    }

    private Intent getPermissionIntent() {
        return new Intent(SplashActivity.this, ActivityPermission.class);
    }

    private Intent getMainIntent() {
        return new Intent(SplashActivity.this, ActivityNavigationView.class);
    }

    private void navigateToMainScreen(Intent intent) {
        startActivity(intent);
        SplashActivity.this.finish();
    }
}
