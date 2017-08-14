package com.h3solution.locationalarm.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.h3solution.locationalarm.R;
import com.h3solution.locationalarm.base.BaseActivity;
import com.h3solution.locationalarm.model.Area;
import com.h3solution.locationalarm.util.Config;
import com.h3solution.locationalarm.util.H3Application;
import com.h3solution.locationalarm.util.SharedPreferencesUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

public class PlacePickerActivity extends BaseActivity implements OnMapReadyCallback {
    private Circle circle;
    public static Marker marker;

    GoogleMap googleMap;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_location_picker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getSupportActionBar() != null;
        getSupportActionBar().hide();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        EventBus.getDefault().register(this);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                refreshCircleOnMap(latLng);
            }
        });

        H3Application.getInstance().stopGetLocation();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void refreshCircleOnMap(LatLng latLng) {
        if (circle != null && marker != null) {
            circle.remove();
            marker.remove();
        }

        marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(false));

        circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(SharedPreferencesUtils.getInstance().getRadius())
                .strokeWidth(5)
                .strokeColor(ContextCompat.getColor(PlacePickerActivity.this, R.color.mapStroke))
                .fillColor(ContextCompat.getColor(PlacePickerActivity.this, R.color.mapSolid))
                .clickable(false));
    }

    @OnClick(R.id.fab_location_done)
    public void onViewClicked() {
        if (circle != null) {
            EventBus.getDefault().post(circle);
//            Area area = new Area(circle.getCenter().latitude, circle.getCenter().longitude, circle.getRadius());
//
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra(Config.AREA_OBJECT_NEW, area);
//            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            Toast.makeText(this, getResources().getString(R.string.null_radius), Toast.LENGTH_SHORT).show();
            //setResult(Activity.RESULT_CANCELED, new Intent());
        }
    }
}