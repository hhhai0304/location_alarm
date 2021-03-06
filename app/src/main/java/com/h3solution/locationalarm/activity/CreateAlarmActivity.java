package com.h3solution.locationalarm.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.h3solution.locationalarm.R;
import com.h3solution.locationalarm.base.BaseActivity;
import com.h3solution.locationalarm.model.Area;
import com.h3solution.locationalarm.util.H3Application;
import com.h3solution.locationalarm.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.realm.Realm;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class CreateAlarmActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    final String CALL_PERMISSION = Manifest.permission.CALL_PHONE;
    final int CALL_PERMISSION_CODE = 110;
    final int CONTACT_PICK_CODE = 111;

    @BindView(R.id.edt_title)
    EditText edtTitle;
    @BindView(R.id.txt_location)
    TextView txtLocation;
    @BindView(R.id.sw_alarm)
    Switch swAlarm;
    @BindView(R.id.btn_pick_alarm_sound)
    Button btnPickAlarmSound;
    @BindView(R.id.chk_vibrate)
    CheckBox chkVibrate;
    @BindView(R.id.ll_alarm)
    RelativeLayout llAlarm;
    @BindView(R.id.sw_call)
    Switch swCall;
    @BindView(R.id.edt_call_contact)
    EditText edtCallContact;
    @BindView(R.id.btn_call_contact)
    Button btnCallContact;
    @BindView(R.id.ll_call)
    LinearLayout llCall;
    @BindView(R.id.sw_sms)
    Switch swSms;
    @BindView(R.id.edt_sms_contact)
    EditText edtSmsContact;
    @BindView(R.id.btn_sms_contact)
    Button btnSmsContact;
    @BindView(R.id.edt_sms_content)
    EditText edtSmsContent;
    @BindView(R.id.ll_sms)
    LinearLayout llSms;

    Realm realm;
    Area area;

    boolean isCreateNew;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_alarm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_activity_create_alarm);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        isCreateNew = true;
        Utils.registerEventBus(this);
        if (isCreateNew) {
            H3Application.getInstance().startGetLocation();
            Timber.i("isCreateNew = TRUE");
            startActivity(new Intent(this, PlacePickerActivity.class));
        }
    }

    private void setupLocationData(Area area) {
        txtLocation.setText(area.getLocationDetail());
        edtTitle.setText(area.getTitle());
        swAlarm.setChecked(area.isAlarm());
        swCall.setChecked(area.isCall());
        swSms.setChecked(area.isSms());

        if (area.isCall()) {
            edtCallContact.setText(area.getCallContact());
        }

        if (area.isSms()) {
            edtSmsContact.setText(area.getSmsContact());
            edtSmsContent.setText(area.getSmsContent());
        }
    }

    private void onCallEnabled() {
        if (swCall.isChecked()) {
            if (EasyPermissions.hasPermissions(this, CALL_PERMISSION)) {
                llCall.setVisibility(View.VISIBLE);
            } else {
                EasyPermissions.requestPermissions(this, "Cho quyền cái đeeee?", CALL_PERMISSION_CODE, CALL_PERMISSION);
            }
        } else {
            llCall.setVisibility(View.GONE);
        }
    }

    private void getContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICK_CODE:
                    edtCallContact.setText(Utils.getFromNumberFromUri(this, data.getData()));
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @OnClick({R.id.txt_location, R.id.btn_pick_alarm_sound, R.id.btn_call_contact, R.id.btn_sms_contact, R.id.fab_done})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_location:
                EventBus.getDefault().postSticky(new LatLng(area.getLatitude(), area.getLongitude()));
                startActivity(new Intent(this, PlacePickerActivity.class));
                break;
            case R.id.btn_pick_alarm_sound:
                break;
            case R.id.btn_call_contact:
                getContact();
                break;
            case R.id.btn_sms_contact:
                break;
            case R.id.fab_done:
                if (isCreateNew) {
                    addOrUpdateArea("");
                } else {
                    addOrUpdateArea(String.valueOf(area.getId()));
                }
                break;
        }
    }

    @OnCheckedChanged({R.id.sw_alarm, R.id.chk_vibrate, R.id.sw_call, R.id.sw_sms})
    public void onCheckedChanged(CompoundButton view) {
        switch (view.getId()) {
            case R.id.sw_alarm:
                if (swAlarm.isChecked()) {
                    llAlarm.setVisibility(View.VISIBLE);
                } else {
                    llAlarm.setVisibility(View.GONE);
                }
                break;
            case R.id.chk_vibrate:
                break;
            case R.id.sw_call:
                onCallEnabled();
                break;
            case R.id.sw_sms:
                if (swSms.isChecked()) {
                    llSms.setVisibility(View.VISIBLE);
                } else {
                    llSms.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void addOrUpdateArea(String id) {
        if (noFunctionEnable()) {
            Toast.makeText(this, getString(R.string.no_function_enable), Toast.LENGTH_SHORT).show();
        } else if (txtLocation.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.no_location_radius), Toast.LENGTH_SHORT).show();
        } else {
            realm.beginTransaction();
            Area area;
            if (TextUtils.isEmpty(id)) {
                area = realm.createObject(Area.class, Utils.getNextId());
            } else {
                area = realm.where(Area.class).equalTo("id", Integer.parseInt(id)).findFirst();
            }
            setObjectData(area);
            realm.commitTransaction();
            finish();
        }
    }

    private boolean noFunctionEnable() {
        return !swAlarm.isChecked() && !swCall.isChecked() && !swSms.isChecked();
    }

    private void setObjectData(Area area) {
        String title = edtTitle.getText().toString().trim();
        String callContact = edtCallContact.getText().toString().trim();
        String smsContact = edtSmsContact.getText().toString().trim();
        String smsContent = edtSmsContent.getText().toString().trim();
        boolean enableAlarm = swAlarm.isChecked();
        boolean enableCall = swCall.isChecked();
        boolean enableSms = swSms.isChecked();

        area.setTitle(title);
        area.setAlarm(enableAlarm);
        area.setCall(enableCall);
        area.setSms(enableSms);
        area.setEnabled(false);

        if (swCall.isChecked()) {
            area.setCallContact(callContact);
        }

        if (swSms.isChecked()) {
            area.setSmsContact(smsContact);
            area.setSmsContent(smsContent);
        }

        area.setLatitude(this.area.getLatitude());
        area.setLongitude(this.area.getLongitude());
        area.setRadius(this.area.getRadius());
    }

    @Override
    protected void onDestroy() {
        Utils.unregisterEventBus(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void locationPicked(Circle circle) {
        if (area == null) {
            Timber.i("locationPicked() -> NULL area");
            area = new Area();
        }
        try {
            area.setLatitude(circle.getCenter().latitude);
            area.setLongitude(circle.getCenter().longitude);
            area.setRadius(circle.getRadius());
        } catch (IllegalStateException e) {
            Timber.e("locationPicked() -> " + e.getMessage());
            realm.beginTransaction();
            area.setLatitude(circle.getCenter().latitude);
            area.setLongitude(circle.getCenter().longitude);
            area.setRadius(circle.getRadius());
            realm.commitTransaction();
        }

        setupLocationData(area);
        Timber.i("locationPicked() " + area.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void editArea(Area area) {
        //TODO bó tay, chuyển về xài intent
        isCreateNew = false;
        this.area = realm.where(Area.class).equalTo("id", area.getId()).findFirst();
        setupLocationData(area);
        Timber.i("editArea() " + area.toString());
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case CALL_PERMISSION_CODE:
                onCallEnabled();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case CALL_PERMISSION_CODE:
                swCall.setChecked(false);
                break;
        }
    }
}