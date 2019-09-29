package com.appman.appmanager.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appman.appmanager.BuildConfig;
import com.appman.appmanager.R;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.util.AppUtil;
import com.appman.appmanager.util.PrintLog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class FragmentDeviceInfo extends BaseFragment {

    private TextView txtOSVersion, txtVersionRelease, txtApiLevel, txtDevice, txtModel,
            txtProduct, txtBrand, txtDisplay, txtCpuAbi1,
            txtHardware, txtId, txtManufacturer, txtSerial, txtUser, txtHost;
    private ScrollView scrollView;
    private AdView adView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideMenuItem();
        initializeMobileAds();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_device_info, container, false);
        initializeViews(v);
        displayDeviceInfo();
        showInterstitialAd();
        return v;
    }

    @Override
    public void onStop() {
        unHideMenuItem();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void initializeViews(View viewItem) {
        super.initializeViews(viewItem);
        scrollView = viewItem.findViewById (R.id.scrollDeviceInfo);
        txtOSVersion = viewItem.findViewById(R.id.txtOSVersion);
        txtVersionRelease = viewItem.findViewById(R.id.txtOSVersionRelease);
        txtApiLevel = viewItem.findViewById(R.id.txtAPILevel);
        txtDevice = viewItem.findViewById(R.id.txtDevice);
        txtModel = viewItem.findViewById(R.id.txtDeviceModel);
        txtProduct = viewItem.findViewById(R.id.txtDeviceProduct);
        txtBrand = viewItem.findViewById(R.id.txtDeviceBrand);
        txtDisplay = viewItem.findViewById(R.id.txtDisplay);
        txtCpuAbi1 = viewItem.findViewById(R.id.txtCpuAbi1);
        txtHardware = viewItem.findViewById(R.id.txtHardware);
        txtId = viewItem.findViewById(R.id.txtID);
        txtManufacturer = viewItem.findViewById(R.id.txtManufacturer);
        txtSerial = viewItem.findViewById(R.id.txtSerial);
        txtUser = viewItem.findViewById(R.id.txtUser);
        txtHost = viewItem.findViewById(R.id.txtHost);

        adView = viewItem.findViewById(R.id.adView);
    }

    private void hideMenuItem() {
        Intent hideMenuIntent = new Intent();
        hideMenuIntent.setAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
        hideMenuIntent.putExtra("Menu", true);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(hideMenuIntent);
    }

    private void initializeMobileAds() {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(getActivity(), initializationStatus -> {
            PrintLog.getInstance().doPrintDebugLog("FragmentDeviceInfo",
                    "Mobile Ads initialization status -> "
                            + initializationStatus.toString());
        });
    }

    private void unHideMenuItem() {
        Intent hideMenuIntent = new Intent();
        hideMenuIntent.setAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
        hideMenuIntent.putExtra("Menu", false);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(hideMenuIntent);
    }

    private void displayDeviceInfo(){

        String _OSVERSION = System.getProperty("os.version");
        String _RELEASE = android.os.Build.VERSION.RELEASE;
        String _APILEVEL = String.valueOf(android.os.Build.VERSION.SDK_INT);
        String _DEVICE = android.os.Build.DEVICE;
        String _MODEL = android.os.Build.MODEL;
        String _PRODUCT = android.os.Build.PRODUCT;
        String _BRAND = android.os.Build.BRAND;
        String _DISPLAY = android.os.Build.DISPLAY;
        String _CPU_ABI = android.os.Build.CPU_ABI;
        String _HARDWARE = android.os.Build.HARDWARE;
        String _ID = android.os.Build.ID;
        String _MANUFACTURER = android.os.Build.MANUFACTURER;
        String _SERIAL = android.os.Build.SERIAL;
        String _USER = android.os.Build.USER;
        String _HOST = android.os.Build.HOST;

        Animation animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.card_animation);
        scrollView.setAnimation(animation);

        txtOSVersion.setText(_OSVERSION);
        txtVersionRelease.setText(_RELEASE);
        txtApiLevel.setText(_APILEVEL);
        txtDevice.setText(_DEVICE);
        txtModel.setText(_MODEL);
        txtProduct.setText(_PRODUCT);
        txtBrand.setText(_BRAND);
        txtDisplay.setText(_DISPLAY);
        txtCpuAbi1.setText(_CPU_ABI);
        txtHardware.setText(_HARDWARE);
        txtId.setText(_ID);
        txtManufacturer.setText(_MANUFACTURER);
        txtSerial.setText(_SERIAL);
        txtUser.setText(_USER);
        txtHost.setText(_HOST);
    }

    private void showInterstitialAd() {
        adView.setVisibility(View.VISIBLE);

        if (BuildConfig.DEBUG) {
            String deviceIdForTestAds = AppUtil.getInstance().getAdMobDeviceId(getActivity());
            Log.e("DeviceInfo", "Hashed device id to load test ads - " + deviceIdForTestAds);

            AdRequest adRequest = new AdRequest.Builder().addTestDevice(deviceIdForTestAds).build();
            adView.loadAd(adRequest);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
        }
    }
}
