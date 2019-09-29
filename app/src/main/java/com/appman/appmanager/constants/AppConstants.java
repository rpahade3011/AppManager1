package com.appman.appmanager.constants;

public class AppConstants {
    /* Default folder name */
    public static final String DEFAULT_FOLDER = "/AppManager";
    public static final String DEFAULT_APK_FOLDER = DEFAULT_FOLDER + "/APK/";
    public static final String DEFAULT_CRASH_LOG_FOLDER = DEFAULT_FOLDER + "/Logs/";
    public static final String DEFAULT_MUSIC_LIBRARY_FOLDER = DEFAULT_FOLDER + "/Music/";

    public static final int ROOT_STATUS_NOT_CHECKED = 0;
    public static final int ROOT_STATUS_ROOTED = 1;
    public static final int ROOT_STATUS_NOT_ROOTED = 2;

    /* Broadcast */
    public static final String ACTION_APP_PUT_TO_FAVORITES_CALLBACK
            = "com.appman.appmanager.constants.PUT_TO_FAVORITES";
    public static final String ACTION_APP_REMOVED_FROM_FAVORITES_CALLBACK
            = "com.appman.appmanager.constants.REMOVED_FROM_FAVORITES";
    public static final String ACTION_FOUND_INSTALLED_APPS_CALLBACK
            = "com.appman.appmanager.constants.FOUND_INSTALLED_APPS";
    public static final String ACTION_FOUND_SYSTEM_APPS_CALLBACK
            = "com.appman.appmanager.constants.FOUND_SYSTEM_APPS";
    public static final String ACTION_RETRY_AGAIN_CALLBACK
            = "com.appman.appmanager.constants.RETRY_AGAIN";
    public static final String ACTION_HIDE_PROGRESS_BAR_CALLBACK =
            "com.appman.appmanager.constants.HIDE_PROGRESS_BAR";
    public static final String ACTION_DEFAULT_FRAGMENT_CALLBACK =
            "com.appman.appmanager.constants.DEFAULT_FRAGMENT";
    public static final String ACTION_EXTRACT_APPLICATION_CALLBACK =
            "com.appman.appmanager.constants.EXTRACT_APPLICATION";
    public static final String ACTION_PERFORM_SEARCH_CALLBACK =
            "com.appman.appmanager.constants.PERFORM_SEARCH";
    public static final String ACTION_SEARCH_RESULT_CALLBACK =
            "com.appman.appmanager.constants.SEARCH_RESULT";
    public static final String ACTION_HIDE_MENU_ITEM_CALLBACK =
            "com.appman.appmanager.constants.HIDE_MENU_ITEM";
    public static final String ACTION_PULL_TO_REFESH_CALLBACK =
            "com.appman.appmanager.constants.PULL_TO_REFESH";
    public static final String ACTION_MUSIC_LIBRARY_COUNT_CALLBACK
            = "MUSIC_LIBRARY_COUNT_CALLBACK";

    /* Broadcast intent key */
    public static final String EXTRA_KEY_INSTALLED_APPS = "KEY_INSTALLED";
    public static final String EXTRA_KEY_SYSTEM_APPS = "KEY_SYSTEM";
    public static final String EXTRA_KEY_FAVORITES_APPS = "KEY_FAVORITES";
    public static final String EXTRA_KEY_MUSIC_LIBRARY = "KEY_MUSIC_LIBRARY";
    public static final String EXTRA_KEY_APP_INFO = "KEY_APP_INFO";
    public static final String EXTRA_KEY_SEARCH_RESULT = "KEY_SEARCH";
    public static final String EXTRA_KEY_MUSIC_COUNT = "KEY_MUSIC_COUNT";

    /* Error key tags */
    public static final String EXTRA_KEY_ERROR_TEXT = "ERROR_TEXT";
    public static final String EXTRA_KEY_ERROR_TYPE = "ERROR_TYPE";

    /* Fragment tags */
    public static final String FRAGMENT_INSTALLED_APPS_TAG = "INSTALLED_APPS";
    public static final String FRAGMENT_SYSTEM_APPS_TAG = "SYSTEM_APPS";
    public static final String FRAGMENT_FAVORITES_APPS_TAG = "FAVORITES_APPS";
    public static final String FRAGMENT_ERROR_LAYOUT_TAG = "ERROR_LAYOUT";
    public static final String FRAGMENT_DEVICE_INFO_LAYOUT_TAG = "DEVICE_INFO";
    public static final String FRAGMENT_MUSIC_LIBRARY_LAYOUT_TAG = "MUSIC_LIBRARY";

    /* Save instance state key */
    public static final String OUT_STATE_INSTALLED_APPS_KEY = EXTRA_KEY_INSTALLED_APPS;
    public static final String OUT_STATE_SYSTEM_APPS_KEY = EXTRA_KEY_SYSTEM_APPS;
    public static final String OUT_STATE_FAV_APPS_KEY = EXTRA_KEY_FAVORITES_APPS;
    public static final String OUT_STATE_MUSIC_LIBRARY_KEY = EXTRA_KEY_MUSIC_LIBRARY;

    /* Day or Night variables */
    public static final int DAY = 1;
    public static final int NIGHT = 0;

    /* App Update Request code */
    public static final int APP_UPDATE_REQ_CODE = 17362;

    /* Internet Connectivity variables */
    public static final String INTERNET_TYPE_WIFI = "Wifi enabled";
    public static final String INTERNET_TYPE_MOBILE_DATA = "Mobile data enabled";
    public static final String INTERNET_TYPE_NOT_CONNECTED = "Not connected to internet";

    /* Intent extras variables */
    public static final String INTENT_EXTRA_NAME_APP_NAME = "app_name";
    public static final String INTENT_EXTRA_NAME_APP_APK = "app_apk";
    public static final String INTENT_EXTRA_NAME_APP_VERSION = "app_version";
    public static final String INTENT_EXTRA_NAME_APP_SOURCE = "app_source";
    public static final String INTENT_EXTRA_NAME_APP_DATA = "app_data";
    public static final String INTENT_EXTRA_NAME_APP_SIZE = "app_size";
    public static final String INTENT_EXTRA_NAME_APP_ICON = "app_icon";
    public static final String INTENT_EXTRA_NAME_APP_SYSTEM = "app_isSystem";
    public static final String INTENT_EXTRA_SHARE_APP_TYPE = "text/plain";
    public static final String INTENT_EXTRA_SHARE_APP_CHOOSER_TEXT = "Share via";

    /* SMS inbox uri */
    public static final String FONT_PATH = "fonts/ProductSans-Regular.ttf";

    /* App type */
    public enum APP_TYPE {
        INSTALLED,
        SYSTEM,
        FAVORITES,
        NONE
    }

    /* Error type */
    public enum ERROR_TYPE {
        NO_DATA,
        ERROR,
        NO_SEARCH_ITEMS
    }

    /* Music library */
    public enum MUSIC_TYPE {
        LIBRARY,
        NO_DATA_FOUND
    }
}
