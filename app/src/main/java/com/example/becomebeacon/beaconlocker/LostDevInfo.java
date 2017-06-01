package com.example.becomebeacon.beaconlocker;

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

    public LostDevInfo() {
        double longitude = 0;
        double latitude = 0;
        String lostDate = "";
        String devAddr = "";
        this.uid = LoginActivity.getUser().getUid();
        this.userName = LoginActivity.getUser().getDisplayName();
    }

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
