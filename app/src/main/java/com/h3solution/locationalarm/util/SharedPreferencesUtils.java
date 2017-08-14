package com.h3solution.locationalarm.util;

import android.content.Context;
import android.content.SharedPreferences;

import timber.log.Timber;

/**
 * SharedPreferences Utils
 * Created by HHHai on 11-05-2017.
 */
public class SharedPreferencesUtils {
    private static SharedPreferencesUtils instance;

    private final String RADIUS = "radius";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static synchronized SharedPreferencesUtils getInstance() {
        if (instance == null) {
            instance = new SharedPreferencesUtils();
        }
        return instance;
    }

    private SharedPreferencesUtils() {
        preferences = H3Application.getInstance().getSharedPreferences("AppSetting", Context.MODE_PRIVATE);
    }

    public void setRadius(double radius) {
        editor = preferences.edit();
        editor.putString(RADIUS, String.valueOf(radius));
        editor.apply();
    }

    public double getRadius() {
        double result = Double.parseDouble(preferences.getString(RADIUS, "200"));
        Timber.i(String.valueOf(result));
        return result;
    }
}