package com.hhly.pay.alipay.util;


import android.app.Activity;
import android.content.SharedPreferences;
import de.robv.android.xposed.XSharedPreferences;

public class PreferencesUtils {

    private static XSharedPreferences instance = null;

    private static XSharedPreferences getInstance() {
        if (instance == null) {
            instance = new XSharedPreferences("com.hhly.pay.alipay", "config");
            instance.makeWorldReadable();
        } else {
            instance.reload();
        }
        return instance;
    }

    public static String getBeiZu() {
        return getInstance().getString("beizu", "");
    }

    public static String getJinEr() {
        return getInstance().getString("jiner", "");
    }

    public static void setBeiZu(Activity activity, String beizu) {
        putString(activity, "beizu", beizu);
    }

    public static void setJinEr(Activity activity, String jiner) {
        putString(activity, "jiner", jiner);
    }

    public static boolean putString(Activity activity, String key, String value) {
        SharedPreferences  mSharedPreferences = activity.getSharedPreferences("config", Activity.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }
}


