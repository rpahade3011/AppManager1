package com.appman.appmanager.util;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

public class TypefaceUtil {
    private static final String TAG = "TypefaceUtil";

    private static TypefaceUtil sInstance = null;

    public static synchronized TypefaceUtil getInstance() {
        if (sInstance == null) {
            sInstance = new TypefaceUtil();
        }
        return sInstance;
    }

    /**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     * @param context                       to work with assets
     * @param defaultFontNameToOverride     for example "monospace"
     * @param customFontFileNameInAssets      file name of the font from assets
     */
    public void overrideFont(Context context,
                             String defaultFontNameToOverride,
                             String customFontFileNameInAssets) {
        Typeface regular = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);
        replaceFont(defaultFontNameToOverride, regular);
    }

    private void replaceFont(String staticTypefaceFieldName,
                             final Typeface newTypeface) {
        try {
            Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException nfe) {
            PrintLog.getInstance().doPrintErrorLog(TAG, "No such field while overriding font: "
                    + nfe.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            PrintLog.getInstance().doPrintErrorLog(TAG, "Illegal access while overriding font: "
                    + e.getLocalizedMessage());
        }
    }
}
