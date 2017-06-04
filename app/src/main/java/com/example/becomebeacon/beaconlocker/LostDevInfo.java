package com.example.becomebeacon.beaconlocker;

import java.text.SimpleDateFormat;

/**
 * Created by heeseung on 2017-05-23.
 */

public class LostDevInfo {
    private double longitude;
    private double latitude;
    private String lostDate;
    private String devAddr;
    private String uid;
    private String userName;
    private String nickNameOfThing;

    public LostDevInfo() {
        longitude = 0;
        latitude = 0;
        lostDate = "";
        devAddr = "";
        nickNameOfThing= "";
        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();
    }

    public LostDevInfo(BleDeviceInfo bdi) {
        longitude = bdi.longitude;
        latitude = bdi.latitude;
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        lostDate = CurDateFormat.format(bdi.lastDate);
        devAddr = bdi.devAddress;
        nickNameOfThing= bdi.nickname;
        this.uid = bdi.uid;
        this.userName = bdi.userName;
    }

    public String getNickNameOfThing(){return nickNameOfThing;}

    public void setNickNameOfThing(String nick){nickNameOfThing = nick;}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLostDate() {
        return lostDate;
    }

    public void setLostDate(String lostDate) {
        this.lostDate = lostDate;
    }

    public String getDevAddr() {
        return devAddr;
    }

    public void setDevAddr(String devAddr) {
        this.devAddr = devAddr;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
