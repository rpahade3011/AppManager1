package com.appman.appmanager.ui.activities.internet;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appman.appmanager.R;
import com.google.android.material.button.MaterialButton;

public class ActivityNoInternetConnection extends AppCompatActivity {
    private MaterialButton buttonGoToWifi;
    private MaterialButton buttonGoToData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setUpParameters();
        super.onCreate(savedInstanceState);
    }

    private void setUpParameters() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.no_internet_connection);
        initComponents();
    }

    private void initComponents(){
        buttonGoToWifi = findViewById (R.id.buttonGoToWifi);
        buttonGoToData = findViewById (R.id.buttonGoToData);

        //Providing Listeners
        buttonGoToWifi.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });
        buttonGoToData.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
        });
    }

}
