package com.h3solution.locationalarm.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.h3solution.locationalarm.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}