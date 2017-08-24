package com.h3solution.locationalarm.activity;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.h3solution.locationalarm.R;
import com.h3solution.locationalarm.adapter.MainAdapter;
import com.h3solution.locationalarm.base.BaseActivity;
import com.h3solution.locationalarm.model.Area;
import com.h3solution.locationalarm.util.Config;
import com.h3solution.locationalarm.util.H3Application;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @BindView(R.id.rv_alarm_list)
    RecyclerView rvAlarmList;

    Realm realm;

    MainAdapter adapter;
    ArrayList<Area> itemList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (EasyPermissions.hasPermissions(this, LOCATION_PERMISSIONS)) {
            EventBus.getDefault().register(this);
            H3Application.getInstance().startGetLocation();
        } else {
            EasyPermissions.requestPermissions(this, "Hello world?", 1, LOCATION_PERMISSIONS);
        } // TEST bitbucket

        setupData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void setupView() {
        itemList = new ArrayList<>();
        adapter = new MainAdapter(this, itemList);

        rvAlarmList.setLayoutManager(new LinearLayoutManager(this));
        rvAlarmList.setAdapter(adapter);
        rvAlarmList.setHasFixedSize(true);
    }

    private void setupData() {
        itemList.clear();
        RealmResults<Area> results = realm.where(Area.class).findAll();
        for (Area area : results) {
            Timber.d(area.toString());
        }
        itemList.addAll(results);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.fab_add)
    public void onViewClicked() {
        H3Application.getInstance().startGetLocation();
        startActivity(new Intent(MainActivity.this, CreateAlarmActivity.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Location location) {
        Config.currentLocation = location;
        H3Application.getInstance().stopGetLocation();
        EventBus.getDefault().unregister(this);
    }
}