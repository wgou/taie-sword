package com.example.aslib.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Map<String, String> cache = new HashMap<>();

    public static void setValue(Context context, String key, String value) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(key, value);
        edit.commit();
        cache.put(key, value);
    }

    public static String getValue(Context context, String key) {
        String value = cache.get(key);
        if (TextUtils.isEmpty(value)) {
            value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
            if (value != null) {
                setValue(context, key, value);
            }
        }
        return value;
    }
}