package com.h3solution.locationalarm.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;

import com.h3solution.locationalarm.service.LocationService;

import io.realm.Realm;
import timber.log.Timber;

/**
 * H3 Application
 * Created by Hai Ho on 09/08/2017.
 */

public class H3Application extends Application {

    private static H3Application instance;

    public static synchronized H3Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        Timber.plant(new Timber.DebugTree());
        instance = this;
    }

    public void startGetLocation() {
        if (!isServiceRunning(LocationService.class)) {
            Timber.i("START get location service");
            startService(new Intent(this, LocationService.class));
        }
    }

    public void stopGetLocation() {
        if (isServiceRunning(LocationService.class)) {
            Timber.i("STOP get location service");
            stopService(new Intent(this, LocationService.class));
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}