package com.h3solution.locationalarm.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.h3solution.locationalarm.R;
import com.h3solution.locationalarm.base.BaseActivity;
import com.h3solution.locationalarm.util.Config;
import com.h3solution.locationalarm.util.H3Application;
import com.h3solution.locationalarm.util.SharedPreferencesUtils;
import com.h3solution.locationalarm.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.OnClick;
import timber.log.Timber;

public class PlacePickerActivity extends BaseActivity implements OnMapReadyCallback {

    private Circle circle;
    private Marker pickedMarker, currentLocationMarker;

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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initFragments();
        Utils.registerEventBus(this);
    }

    private void initFragments() {
        // Google Maps fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Search place fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.pac_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Timber.i("Place: " + place.getName());
                search(place.getName().toString().trim());
            }

            @Override
            public void onError(Status status) {
                Timber.e("An error occurred: " + status.getStatusMessage());
                Toast.makeText(PlacePickerActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMyLocationEnabled(false);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                refreshCircleOnMap(latLng);
            }
        });
        LatLng prevPicked = EventBus.getDefault().getStickyEvent(LatLng.class);
        if (prevPicked != null) {
            refreshCircleOnMap(prevPicked);
        }
        moveCameraToCurrentLocation();
    }

    private void search(String keyword) {
        Geocoder geocoder = new Geocoder(this);
        if (!TextUtils.isEmpty(keyword)) {
            try {
                List<Address> addressList = geocoder.getFromLocationName(keyword, 1);

                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                moveCameraToCurrentLocation(latLng);
            } catch (Exception e) {
                Timber.e(e.getMessage());
                moveCameraToCurrentLocation();
            }
        }
    }

    private void moveCameraToCurrentLocation() {
        LatLng latLng = new LatLng(Config.currentLocation.getLatitude(), Config.currentLocation.getLongitude());
        moveCameraToCurrentLocation(latLng);
    }

    private void moveCameraToCurrentLocation(LatLng latLng) {
        if (latLng != null) {
            if (currentLocationMarker == null) {
                currentLocationMarker = googleMap.addMarker(markerType(latLng));
            }
            currentLocationMarker.setPosition(latLng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Location location) {
        Config.currentLocation = location;
        H3Application.getInstance().stopGetLocation();
        Utils.unregisterEventBus(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.unregisterEventBus(this);
    }

    private void refreshCircleOnMap(LatLng latLng) {
        if (circle != null && pickedMarker != null) {
            circle.remove();
            pickedMarker.remove();
        }

        pickedMarker = googleMap.addMarker(new MarkerOptions()
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

    public static MarkerOptions markerType(LatLng latLng) {
        MarkerOptions option = new MarkerOptions();
        option.position(latLng);
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        option.draggable(false);
        return option;
    }

    @OnClick({R.id.fab_location_done, R.id.fab_my_location})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_location_done:
                if (circle != null) {
                    EventBus.getDefault().post(circle);
                    finish();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.null_radius), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fab_my_location:
                moveCameraToCurrentLocation();
                break;
        }
    }
}