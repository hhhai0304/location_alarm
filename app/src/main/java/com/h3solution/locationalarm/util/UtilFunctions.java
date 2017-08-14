package com.h3solution.locationalarm.util;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

import com.h3solution.locationalarm.model.Area;

import java.util.ArrayList;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Utility Class
 * Created by HHHai on 11-05-2017.
 */
public class UtilFunctions {

    public static ArrayList<Area> enabledAreas = new ArrayList<>();

    public static int getNextId() {
        try {
            Realm realm = Realm.getDefaultInstance();
            Number number = realm.where(Area.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static void check(Location myLocation) {
        if (enabledAreas == null) {
            enabledAreas = new ArrayList<>();
        }

        if (!enabledAreas.isEmpty()) {
            for (Area area : enabledAreas) {
                Timber.i(String.valueOf(area.isCall()));
                if (area.isCall() && isInArea(myLocation, area.getLatitude(), area.getLongitude(), area.getRadius())) {
                    actionCall(area);
                }
            }
        }
    }

    public static void actionCall(Area area) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + area.getCallContact()));
        if (ActivityCompat.checkSelfPermission(H3Application.getInstance(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("No call permission");
            return;
        }
        H3Application.getInstance().startActivity(intent);
        onArrived(area);
    }

    private static void onArrived(Area area) {
        enabledAreas.remove(area);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Area result = realm.where(Area.class)
                .equalTo("id", area.getId())
                .findFirst();
        result.setEnabled(false);
        realm.commitTransaction();
        realm.close();

        if (enabledAreas.isEmpty()) {
            H3Application.getInstance().stopGetLocation();
        }
    }

    public static boolean isInArea(Location myLocation, double latitude, double longitude, double radius) {
        float[] distance = new float[2];
        Location.distanceBetween(latitude, longitude,
                myLocation.getLatitude(), myLocation.getLongitude(), distance);

        return distance[0] < radius;
    }
}