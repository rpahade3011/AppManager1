package com.appman.appmanager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.appman.appmanager.constants.AppConstants;

public class InternetConnection {

    private static InternetConnection sInstance = null;
    private static final int TYPE_WIFI = 1;
    private static final int TYPE_MOBILE = 2;
    private static final int TYPE_NOT_CONNECTED = 0;

    private InternetConnection() {}

    public static InternetConnection getInstance() {
        if (sInstance == null) {
            sInstance = new InternetConnection();
        }
        return sInstance;
    }

    /**
     * THIS METHOD WILL CHECK THE INTERNET CONNECTION OF THE DEVICE AND RETURN THE BOOLEAN VALUE
     *
     * @param context
     * @return
     */
    public boolean isInternetConnectionAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
            if (networkInfos != null) {
                for (int i = 0; i < networkInfos.length; i++) {
                    if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getConnectivityStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

    public String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        switch (conn) {
            case TYPE_WIFI:
                status = AppConstants.INTERNET_TYPE_WIFI;
                break;
            case TYPE_MOBILE:
                status = AppConstants.INTERNET_TYPE_MOBILE_DATA;
                break;
            case TYPE_NOT_CONNECTED:
                status = AppConstants.INTERNET_TYPE_NOT_CONNECTED;
                break;
        }
        return status;
    }
}
