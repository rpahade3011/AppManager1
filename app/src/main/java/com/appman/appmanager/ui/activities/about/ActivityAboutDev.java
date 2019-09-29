package com.appman.appmanager.ui.activities.about;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.appman.appmanager.R;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.ui.activities.base.BaseActivity;
import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

public class ActivityAboutDev extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about_dev);
        initializeToolbar();
        initializeViews();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    protected void initializeToolbar() {
        super.initializeToolbar();
        Toolbar profileToolbar = findViewById(R.id.toolbarAboutDev);
        setSupportActionBar(profileToolbar);
        setToolbarTypeface(profileToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.action_about));
            profileToolbar.setNavigationOnClickListener(view -> onBackPressed());
            profileToolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    protected void setToolbarTypeface(Toolbar toolbar) {
        super.setToolbarTypeface(toolbar);
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

    @Override
    protected void initializeViews() {
        super.initializeViews();
        final FrameLayout flHolder = findViewById(R.id.aboutme);
        if (flHolder != null) {
            flHolder.addView(getAboutDev());
        }
    }

    private void applyFont(TextView mToolbarTextView) {
        mToolbarTextView.setTypeface(Typeface.createFromAsset(ActivityAboutDev.this.getAssets(),
                AppConstants.FONT_PATH));
    }

    private AboutView getAboutDev() {
        AboutBuilder aboutBuilder = AboutBuilder.with(this)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .setAppTitle(R.string.app_description)
                .setPhoto(R.mipmap.about_rudraksh_pahade_profile_pic)
                .setLinksAnimated(false)
                .setDividerDashGap(13)
                .setName("Rudraksh Pahade")
                .setNameColor(R.color.colorAccentSecondary)
                .setSubTitle("Mobile Developer")
                .setLinksColumnsCount(3)
                .setBrief("I'm warmed of mobile technologies. Ideas maker, curious and nature lover.")
                .addGooglePlayStoreLink("6251285879755104834")
                .addGitHubLink("rpahade3011")
                .addFacebookLink("rudraksh.pahade")
                .addTwitterLink("pahade_rudraksh")
                .addInstagramLink("rudrakshpahade")
                .addGooglePlusLink("109312616470328191163")
                .addLinkedInLink("rudraksh-pahade-752b3b3a")
                .addEmailLink("rudraksh3011@gmail.com")
                .addWhatsappLink("Rudraksh", "+919028411974")
                .addSkypeLink("rudraksh.pahade")
                .addGoogleLink("rudraksh3011")
                .addFiveStarsAction()
                .addMoreFromMeAction("Rudraksh+Pahade")
                .setVersionNameAsAppSubTitle()
                .addShareAction(R.string.app_name)
                .addUpdateAction()
                .setActionsColumnsCount(3)
                .addFeedbackAction("rudraksh3011@gmail.com");
        return aboutBuilder.build();
    }
}
