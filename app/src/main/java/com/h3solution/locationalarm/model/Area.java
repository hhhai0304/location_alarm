package com.h3solution.locationalarm.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Area model
 * Created by HHHai on 12-05-2017.
 */
public class Area extends RealmObject implements Parcelable {
    @PrimaryKey
    private int id;
    private double latitude;
    private double longitude;
    private double radius;

    private boolean isEnabled;
    private boolean isAlarm;
    private boolean isCall;
    private boolean isSms;

    private String title;
    private String callContact;
    private String smsContact;
    private String smsContent;

    public String getLocationDetail() {
        return "Latitude: " + latitude + "\nLongitude: " + longitude + "\nRadius: " + radius;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public void setAlarm(boolean alarm) {
        isAlarm = alarm;
    }

    public boolean isCall() {
        return isCall;
    }

    public void setCall(boolean call) {
        isCall = call;
    }

    public boolean isSms() {
        return isSms;
    }

    public void setSms(boolean sms) {
        isSms = sms;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCallContact() {
        return callContact;
    }

    public void setCallContact(String callContact) {
        this.callContact = callContact;
    }

    public String getSmsContact() {
        return smsContact;
    }

    public void setSmsContact(String smsContact) {
        this.smsContact = smsContact;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.radius);
        dest.writeByte(this.isEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isAlarm ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isCall ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSms ? (byte) 1 : (byte) 0);
        dest.writeString(this.title);
        dest.writeString(this.callContact);
        dest.writeString(this.smsContact);
        dest.writeString(this.smsContent);
    }

    public Area() {
    }

    protected Area(Parcel in) {
        this.id = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.radius = in.readDouble();
        this.isEnabled = in.readByte() != 0;
        this.isAlarm = in.readByte() != 0;
        this.isCall = in.readByte() != 0;
        this.isSms = in.readByte() != 0;
        this.title = in.readString();
        this.callContact = in.readString();
        this.smsContact = in.readString();
        this.smsContent = in.readString();
    }

    public static final Parcelable.Creator<Area> CREATOR = new Parcelable.Creator<Area>() {
        @Override
        public Area createFromParcel(Parcel source) {
            return new Area(source);
        }

        @Override
        public Area[] newArray(int size) {
            return new Area[size];
        }
    };
}